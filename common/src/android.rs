#![allow(non_snake_case)]

use crate::start_arti_proxy;

use tracing_subscriber::fmt::Subscriber;
use tracing_subscriber::prelude::*;
use tracing::{info};

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

// this is supposed to forward artis built-in logging to logcat
// https://gitlab.torproject.org/tpo/core/arti/-/blob/main/doc/Android.md#debugging-and-stability
#[no_mangle]
pub extern "system" fn Java_info_guardianproject_arti_ArtiJNI_initLogging(
                                             _env: JNIEnv,
                                             _class: JClass) {

    let layer = tracing_android::layer("rust.arti").expect("couldn't create tracing layer");
    Subscriber::new()
        .with(layer)
        .init(); // this must be called only once, otherwise your app will probably crash
    info!("arti-android native logging initialized");
}


