use anyhow::{anyhow, Result};
use async_std::task::sleep;
use std::net::SocketAddr;
use std::sync::{Arc, Mutex};
use std::time::Duration;
use std::{fmt, thread};
use tor_linkspec::TransportIdError;
use tracing::{info, warn};

use arti::{dns, exit, reload_cfg, socks, ArtiConfig};
use arti_client::config::pt::TransportConfigBuilder;
use arti_client::config::{PtTransportName, TorClientConfigBuilder};
use arti_client::{TorClient, TorClientConfig};
use tor_config::{CfgPath, ConfigurationSources, Listen};
use tor_rtcompat::{BlockOn, PreferredRuntime, Runtime};

use tracing_subscriber::fmt::{Layer, Subscriber};
use tracing_subscriber::layer::SubscriberExt;
use tracing_subscriber::util::SubscriberInitExt;

#[macro_use]
extern crate lazy_static;

// TODO: look into
//   - https://crates.io/crates/lazy_static
//   - https://doc.rust-lang.org/std/sync/struct.Mutex.html
// for sharing data between threads/functions

lazy_static! {
    static ref STATE: Mutex<AMExState> = Mutex::new(AMExState::Uninitialized);
}

enum AMExState {
    Uninitialized,
    Initialized,
    Starting,
    Running,
    Stopping,
    Stopped,
}

impl fmt::Display for AMExState {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self {
            AMExState::Initialized => write!(f, "Initialized"),
            AMExState::Running => write!(f, "Running"),
            AMExState::Starting => write!(f, "Starting"),
            AMExState::Stopped => write!(f, "Stopped"),
            AMExState::Stopping => write!(f, "Stopping"),
            AMExState::Uninitialized => write!(f, "Uninitialized"),
        }
    }
}

fn start_arti_proxy<F>(
    cache_dir: &str,
    state_dir: &str,
    obfs4_port: u16,
    snowflake_port: u16,
    obfs4proxy_path: Option<&str>,
    bridge_lines: Option<&str>,
    socks_port: u16,
    dns_port: u16,
    log_fn: F,
) -> Result<String>
where
    F: Fn(&[u8]) + Send + Sync + 'static,
{
    _init_log_subscriber(log_fn);
    _configure_and_run_arti_proxy(
        cache_dir,
        state_dir,
        obfs4_port,
        snowflake_port,
        obfs4proxy_path,
        bridge_lines,
        socks_port,
        dns_port,
    );

    Ok("arti-mobile-ex proxy init".to_owned())
}

fn _init_log_subscriber<F>(log_fn: F)
where
    F: Fn(&[u8]) + Send + Sync + 'static,
{
    if let Ok(mut state) = STATE.lock() {
        if let AMExState::Uninitialized = *state {
            let log_fn = Arc::new(log_fn);
            let log = Layer::new().with_writer(move || CallbackWriter::new(log_fn.clone()));
            Subscriber::builder().finish().with(log).init();

            *state = AMExState::Initialized;
            info!("AMEx: state changed to {}", *state);
        } else {
            info!("AMEx: logging already initialied");
        }
    }
}

fn _configure_and_run_arti_proxy(
    cache_dir: &str,
    state_dir: &str,
    obfs4_port: u16,
    snowflake_port: u16,
    obfs4proxy_path: Option<&str>,
    bridge_lines: Option<&str>,
    socks_port: u16,
    dns_port: u16,
) {
    if let Ok(mut state) = STATE.lock() {
        if let AMExState::Initialized | AMExState::Stopped = *state {
            *state = AMExState::Starting;
            info!("AMEx: state changed to {}", *state);
        } else {
            info!(
                "AMEx: _configure_and_run_arti_proxy called from wrong state: {} (expected: Initialized or Stopped)",
                *state
            );
            return;
        }
    } else {
        info!("AMEx: could not lock state, aborting _configure_and_run_arti_proxy()");
        return;
    }

    let runtime = PreferredRuntime::create().expect("Could not create Tor runtime.");
    let config_sources = ConfigurationSources::default();
    let arti_config = ArtiConfig::default();
    let mut client_config_builder = TorClientConfigBuilder::from_directories(state_dir, cache_dir);

    let ptn: Result<PtTransportName, TransportIdError> = "snowflake".parse();
    ptn.unwrap_or_else(|err| {
        panic!("err snowflake fuckup {:?}", err);
    });

    // configure transport for unmanaged lyrebrid/obfs4
    if obfs4_port > 0 {
        let mut transport = TransportConfigBuilder::default();
        transport
            .protocols(vec!["obfs4".parse().unwrap()])
            .proxy_addr(SocketAddr::new("127.0.0.1".parse().unwrap(), 47300));
        client_config_builder.bridges().transports().push(transport);
    }

    // configure transport for unmanaged snowflake
    if snowflake_port > 0 {
        let mut transport = TransportConfigBuilder::default();
        transport
            .protocols(vec!["snowflake".parse().unwrap()])
            .proxy_addr(SocketAddr::new(
                "127.0.0.1".parse().unwrap(),
                snowflake_port,
            ));
        client_config_builder.bridges().transports().push(transport);
    }

    // TODO: make this go away?
    if let Some(o4p) = obfs4proxy_path {
        let mut transport = TransportConfigBuilder::default();
        transport
            .protocols(vec!["obfs4".parse().unwrap()])
            .path(CfgPath::new(o4p.into()))
            .run_on_startup(true);
        client_config_builder.bridges().transports().push(transport);
    }
    if let Some(l) = bridge_lines {
        for bridge_line in l.split("\n") {
            client_config_builder
                .bridges()
                .bridges()
                .push(bridge_line.parse().unwrap());
        }
    }

    thread::spawn(move || {
        runtime
            .clone()
            .block_on(_run(
                runtime,
                Listen::new_localhost(socks_port),
                Listen::new_localhost(dns_port),
                config_sources,
                arti_config,
                client_config_builder.build().unwrap(),
            ))
            .expect("Could not start Arti.");
    });
}

/// Shorthand for a boxed and pinned Future.
// https://gitlab.torproject.org/tpo/core/arti/-/blob/9f608f9b/crates/arti/src/lib.rs#L124
type PinnedFuture<T> = std::pin::Pin<Box<dyn futures::Future<Output = T>>>;

// modified run function based on
// https://gitlab.torproject.org/tpo/core/arti/-/blob/9f608f9b/crates/arti/src/lib.rs#L180
async fn _run<R: Runtime>(
    runtime: R,
    socks_listen: Listen,
    dns_listen: Listen,
    config_sources: ConfigurationSources,
    arti_config: ArtiConfig,
    client_config: TorClientConfig,
) -> Result<()> {
    if let Ok(state) = STATE.lock() {
        if let AMExState::Starting = *state {
            // no action required here
        } else {
            let e = format!(
                "AMEx: _configure_and_run_arti_proxy called from wrong state: {} (expected: Starting)",
                *state
            );
            return Err(anyhow!(e));
        }
    } else {
        let e = "AMEx: could not lock state, aborting _configure_and_run_arti_proxy()";
        return Err(anyhow!(e));
    }

    // Using OnDemand arranges that, while we are bootstrapping, incoming connections wait
    // for bootstrap to complete, rather than getting errors.
    use arti_client::BootstrapBehavior::OnDemand;
    use futures::FutureExt;

    // TODO: disabled rpc for now
    // #[cfg(feature = "rpc")]
    // let rpc_path = {
    //     if let Some(path) = &arti_config.rpc().rpc_listen {
    //         let path = path.path()?;
    //         let parent = path
    //             .parent()
    //             .ok_or(anyhow::anyhow!("No parent directory for rpc_listen path?"))?;
    //         client_config
    //             .fs_mistrust()
    //             .verifier()
    //             .make_secure_dir(parent)?;
    //         // It's just a unix thing; if we leave this sitting around, binding to it won't
    //         // work right.  There is probably a better solution.
    //         if path.exists() {
    //             std::fs::remove_file(&path)?;
    //         }

    //         Some(path)
    //     } else {
    //         None
    //     }
    // };

    let client_builder = TorClient::with_runtime(runtime.clone())
        .config(client_config)
        .bootstrap_behavior(OnDemand);
    let client = client_builder.create_unbootstrapped_async().await?;

    #[allow(unused_mut)]
    let mut reconfigurable_modules: Vec<Arc<dyn reload_cfg::ReconfigurableModule>> = vec![
        Arc::new(client.clone()),
        // Arc::new(reload_cfg::Application::new(arti_config.clone())),
    ];

    // TODO: disabled onion service support for now
    // #[cfg(feature = "onion-service-service")]
    // {
    //     let onion_services =
    //         onion_proxy::ProxySet::launch_new(&client, arti_config.onion_services.clone())?;
    //     reconfigurable_modules.push(Arc::new(onion_services));
    // }

    // We weak references here to prevent the thread spawned by watch_for_config_changes from
    // keeping these modules alive after this function exits.
    //
    // NOTE: reconfigurable_modules stores the only strong references to these modules,
    // so we must keep the variable alive until the end of the function
    let weak_modules = reconfigurable_modules.iter().map(Arc::downgrade).collect();
    reload_cfg::watch_for_config_changes(
        client.runtime(),
        config_sources,
        &arti_config,
        weak_modules,
    )?;

    // #[cfg(all(feature = "rpc", feature = "tokio"))]
    // let rpc_mgr = {
    //     // TODO RPC This code doesn't really belong here; it's just an example.
    //     if let Some(listen_path) = rpc_path {
    //         // TODO Conceivably this listener belongs on a renamed "proxy" list.
    //         Some(rpc::launch_rpc_listener(
    //             &runtime,
    //             listen_path,
    //             client.clone(),
    //         )?)
    //     } else {
    //         None
    //     }
    // };

    let mut proxy: Vec<PinnedFuture<(Result<()>, &str)>> = Vec::new();
    if !socks_listen.is_empty() {
        let runtime = runtime.clone();
        let client = client.isolated_client();
        proxy.push(Box::pin(async move {
            let res = socks::run_socks_proxy(
                runtime,
                client,
                socks_listen,
                // #[cfg(all(feature = "rpc", feature = "tokio"))]
                // rpc_mgr,
            )
            .await;
            (res, "SOCKS")
        }));
    }

    // #[cfg(feature = "dns-proxy")]
    if !dns_listen.is_empty() {
        let runtime = runtime.clone();
        let client = client.isolated_client();
        proxy.push(Box::pin(async move {
            let res = dns::run_dns_resolver(runtime, client, dns_listen).await;
            (res, "DNS")
        }));
    }

    if proxy.is_empty() {
        warn!("No proxy port set; specify -p PORT (for `socks_port`) or -d PORT (for `dns_port`). Alternatively, use the `socks_port` or `dns_port` configuration option.");
        return Ok(());
    }
    use anyhow::Context;

    let proxy = futures::future::select_all(proxy).map(|(finished, _index, _others)| finished);
    futures::select!(
        r = exit::wait_for_ctrl_c().fuse()
            => r.context("waiting for termination signal"),
        r = proxy.fuse()
            => r.0.context(format!("{} proxy failure", r.1)),
        r = async {
            client.bootstrap().await?;
            info!("Sufficiently bootstrapped; system SOCKS now functional.");

            if let Ok(mut state) = STATE.lock(){
                *state = AMExState::Running;
                info!("AMEx: state changed to {}", *state);
            }

            loop {
                sleep(Duration::from_millis(200)).await;
                if let Ok(state) = STATE.lock() {
                    if let AMExState::Stopping = *state {
                        info!("AMEx: Stopping requst detected, stopping proxy");
                        break;
                    }
                }
            }

            let r: Result<(), anyhow::Error> = Result::Ok(());
            r
        }.fuse()
            => r.context("bootstrap"),
    )?;

    // The modules can be dropped now, because we are exiting.
    drop(reconfigurable_modules);

    if let Ok(mut state) = STATE.lock() {
        *state = AMExState::Stopped;
        info!("AMEx: state changed to {}", *state);
    }

    Ok(())
}

#[derive(Clone)]
struct CallbackWriter<F> {
    func: Arc<F>,
}

impl<F> CallbackWriter<F>
where
    F: Fn(&[u8]) + Send + Sync + 'static,
{
    pub fn new(callback: Arc<F>) -> Self {
        CallbackWriter { func: callback }
    }
}

impl<F> std::io::Write for CallbackWriter<F>
where
    F: Fn(&[u8]) + Send + Sync + 'static,
{
    fn write(&mut self, buf: &[u8]) -> std::io::Result<usize> {
        (self.func)(buf);
        return Ok(buf.len());
    }

    fn flush(&mut self) -> std::io::Result<()> {
        Ok(())
    }
}

fn stop_arti_proxy() {
    if let Ok(mut state) = STATE.lock() {
        if let AMExState::Running = *state {
            *state = AMExState::Stopping;
            info!("AMEx: state changed to {}", *state);
        }
    }
}

/// Expose the JNI interface for Android
#[cfg(target_os = "android")]
pub mod android;

/// Expose the native interface for iOS
#[cfg(any(target_os = "ios", target_os = "macos"))]
pub mod apple;
