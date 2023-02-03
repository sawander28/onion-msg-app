#![allow(non_snake_case)]

use crate::start_arti_proxy;

use anyhow::Result;

use std::sync::Once;
use tracing_subscriber::fmt::Subscriber;
use tracing_subscriber::prelude::*;

use jni::objects::{JClass, JString};
use jni::sys::jstring;
use jni::JNIEnv;

/// Create a static method myMethod on class net.example.MyClass
#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_info_guardianproject_arti_ArtiJNI_startArtiProxyJNI(
                                             env: JNIEnv,
                                             _class: JClass,
                                             cacheDir: JString,
                                             stateDir: JString)
                                             -> jstring {

    // if logger initialization failed, there isn't much we can do, not even log it.
    // it shouldn't stop Arti from functionning however!
    let _ = init_logger();

    let _result = match start_arti_proxy(
        &env.get_string(cacheDir)
            .expect("cache_dir is invalid")
            .to_string_lossy(),
        &env.get_string(stateDir)
            .expect("state_dir is invalid")
            .to_string_lossy(),
    ) {
        Ok(res) => format!("Output: {}", res),
        Err(e) => format!("Error: {}", e),
    };

    let output = env.new_string(format!("arti-native proxy initialized")).expect("Couldn't create java string!");
    return output.into_inner()

}

static LOGGER: Once = Once::new();

fn init_logger() -> Result<()> {
    if LOGGER.is_completed() {
        let layer = tracing_android::layer("rust.arti")?;
        LOGGER.call_once(|| Subscriber::new().with(layer).init());
    }
    Ok(())
}

