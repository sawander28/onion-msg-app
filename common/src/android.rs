#![allow(non_snake_case)]

use crate::start_arti_proxy;

use anyhow::Result;

use std::sync::Once;
use tracing_subscriber::fmt::Subscriber;
use tracing_subscriber::prelude::*;

use jni::objects::{JClass, JString};
use jni::sys::{jint, jstring};
use jni::JNIEnv;

/// Create a static method myMethod on class net.example.MyClass
#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_info_guardianproject_arti_ArtiJNI_startArtiProxyJNI(
    env: JNIEnv,
    _class: JClass,
    cacheDir: JString,
    stateDir: JString,
    socks_port: jint,
    dns_port: jint,
) -> jstring {
    // if logger initialization failed, there isn't much we can do, not even log it.
    // it shouldn't stop Arti from functioning however!
    let _ = init_logger();

    let result = match start_arti_proxy(
        &env.get_string(cacheDir)
            .expect("cache_dir is invalid")
            .to_string_lossy(),
        &env.get_string(stateDir)
            .expect("state_dir is invalid")
            .to_string_lossy(),
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

static LOGGER: Once = Once::new();

fn init_logger() -> Result<()> {
    if LOGGER.is_completed() {
        let layer = tracing_android::layer("rust.arti")?;
        LOGGER.call_once(|| Subscriber::new().with(layer).init());
    }
    Ok(())
}

/// Android 5.0 to 6.0 misses this function, which prevents Arti from running. This is a translation
/// to Rust of Musl implementation. If you don't plan to support anything below Android 7.0, you
/// should probably not copy this code.
/// It might be possible to support Android 4.4 and below with the same trick applied to more
/// functions (at least create_epoll1), but this might not be worth the effort.
#[no_mangle]
pub unsafe extern "C" fn lockf(fd: libc::c_int, cmd: libc::c_int, len: libc::off_t) -> libc::c_int {
    use libc::*;
    let mut l = flock {
        l_type: F_WRLCK as i16,
        l_whence: SEEK_CUR as i16,
        l_len: len,
        l_pid: 0,
        l_start: 0,
    };
    match cmd {
        F_TEST => {
            l.l_type = F_RDLCK as i16;
            if fcntl(fd, F_GETLK, &l) < 0 {
                return -1;
            }
            if l.l_type == F_UNLCK as i16 || l.l_pid == getpid() {
                return 0;
            }
            *__errno() = EACCES;
            -1
        }
        F_ULOCK => {
            l.l_type = F_UNLCK as i16;
            fcntl(fd, F_SETLK, &l)
        }
        F_TLOCK => fcntl(fd, F_SETLK, &l),
        F_LOCK => fcntl(fd, F_SETLKW, &l),
        _ => {
            *__errno() = EINVAL;
            -1
        }
    }
}
