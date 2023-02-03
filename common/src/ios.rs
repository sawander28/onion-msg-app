#![allow(non_snake_case)]

use crate::run_arti;

use anyhow::Result;

use std::ffi::{CStr, CString};
use std::os::raw::c_char;

use std::sync::Once;
use tracing_subscriber::fmt::Subscriber;
use tracing_subscriber::prelude::*;

#[no_mangle]
pub extern "C" fn start_arti_proxy(state_dir: *const c_char, cache_dir: *const c_char) -> *mut c_char {
    let _ = init_logger();

    let state_dir = unsafe { CStr::from_ptr(state_dir) }.to_string_lossy();
    let cache_dir = unsafe { CStr::from_ptr(cache_dir) }.to_string_lossy();
    let result = match start_arti_proxy(&state_dir, &cache_dir) {
        Ok(res) => format!("Output: {}", res),
        Err(e) => format!("Error: {}", e),
    };

    CString::new(result).unwrap().into_raw()
}

#[no_mangle]
pub extern "C" fn my_rust_function_free_result(s: *mut c_char) {
    unsafe {
        if !s.is_null() {
            drop(CString::from_raw(s));
        }
    }
}

static LOGGER: Once = Once::new();

fn init_logger() -> Result<()> {
    if LOGGER.is_completed() {
        let layer = tracing_oslog::OsLogger::new("rust.arti", "default");
        LOGGER.call_once(|| Subscriber::new().with(layer).init());
    }
    Ok(())
}
