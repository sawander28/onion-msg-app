use anyhow::Result;
use std::sync::Arc;

use std::thread;

use arti::{run, ArtiConfig};
use arti_client::config::TorClientConfigBuilder;
use tor_config::ConfigurationSources;
use tor_rtcompat::{BlockOn, PreferredRuntime};

use tracing_subscriber::fmt::{Layer, Subscriber};
use tracing_subscriber::layer::SubscriberExt;
use tracing_subscriber::util::SubscriberInitExt;

fn start_arti_proxy<F>(
    cache_dir: &str,
    state_dir: &str,
    socks_port: u16,
    dns_port: u16,
    log_fn: F,
) -> Result<String>
where
    F: Fn(&[u8]) + Send + Sync + 'static,
{
    let log_fn = Arc::new(log_fn);
    let log = Layer::new().with_writer(move || CallbackWriter::new(log_fn.clone()));
    Subscriber::builder().finish().with(log).init();

    let runtime = PreferredRuntime::create().expect("Could not create Tor runtime.");
    let config_sources = ConfigurationSources::default();
    let arti_config = ArtiConfig::default();
    let client_config = TorClientConfigBuilder::from_directories(state_dir, cache_dir)
        .build()
        .expect("Could not build Tor config.");

    thread::spawn(move || {
        runtime
            .clone()
            .block_on(run(
                runtime,
                socks_port,
                dns_port,
                config_sources,
                arti_config,
                client_config,
            ))
            .expect("Could not start Arti.");
    });

    Ok("arti-mobile-ex proxy init".to_owned())
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

/// Expose the JNI interface for Android
#[cfg(target_os = "android")]
pub mod android;

/// Expose the native interface for iOS
#[cfg(any(target_os = "ios", target_os = "macos"))]
pub mod apple;
