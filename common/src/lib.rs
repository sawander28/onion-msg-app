use anyhow::Result;
use futures::{AsyncReadExt, AsyncWriteExt};

use arti::socks::run_socks_proxy;
use arti_client::{config::TorClientConfigBuilder, TorClient};
use futures::task::SpawnExt;

fn start_arti_proxy(cache_dir: &str, state_dir: &str) -> Result<String> {

    let runtime = tor_rtcompat::PreferredRuntime::create().expect("could not create tor runtime");

    let config = TorClientConfigBuilder::from_directories(state_dir, cache_dir)
        .build()
        .expect("could not build tor config");

    let client_builder = TorClient::with_runtime(runtime.clone()).config(config);
    let client = client_builder.create_unbootstrapped().expect("could not build tor client");

    runtime.clone().spawn(async {
        run_socks_proxy(runtime, client, 9150).await.expect("could not run socks proxy");
    }).expect("could not spawn");

    let output = env.new_string(format!("arti-native proxy initialized")).expect("Couldn't create java string!");
    return output.into_inner()

}

/// Expose the JNI interface for Android
#[cfg(target_os = "android")]
pub mod android;

/// Expose the native interface for iOS
#[cfg(target_os = "ios")]
pub mod ios;
