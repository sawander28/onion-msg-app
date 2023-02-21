#![allow(non_snake_case)]

use crate::start_arti_proxy;

use tracing::info;
use tracing_subscriber::fmt::Subscriber;
use tracing_subscriber::prelude::*;

use jni::objects::{JClass, JString};
use jni::sys::{jint, jstring};
use jni::JNIEnv;

/// Create a static method myMethod on class net.example.MyClass
#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_info_guardianproject_arti_ArtiJNI_startArtiProxyJNI<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    cacheDir: JString<'local>,
    stateDir: JString<'local>,
    socks_port: jint,
    dns_port: jint,
) -> jstring {
    let cacheDir: String = env
        .get_string(&cacheDir)
        .expect("cache_dir is invalid")
        .to_string_lossy()
        .into_owned();
    let stateDir: String = env
        .get_string(&stateDir)
        .expect("state_dir is invalid")
        .to_string_lossy()
        .into_owned();

    let result = match start_arti_proxy(
        &cacheDir,
        &stateDir,
        socks_port as u16,
        dns_port as u16,
        move |_buf: &[u8]| {},
    ) {
        Ok(res) => format!("Output: {}", res),
        Err(e) => format!("Error: {}", e),
    };

    env.new_string(result)
        .expect("Couldn't create Java string!")
        .into_raw()
}

// this is supposed to forward arti's built-in logging to logcat
// https://gitlab.torproject.org/tpo/core/arti/-/blob/main/doc/Android.md#debugging-and-stability
#[no_mangle]
pub extern "system" fn Java_info_guardianproject_arti_ArtiJNI_initLogging<'local>(
    mut _env: JNIEnv<'local>,
    _class: JClass<'local>,
) {
    let layer = tracing_android::layer("rust.arti").expect("couldn't create tracing layer");
    Subscriber::new().with(layer).init(); // this must be called only once, otherwise your app will probably crash
    info!("arti-android native logging initialized");
}
