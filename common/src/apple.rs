use crate::start_arti_proxy;

use std::ffi::{CStr, CString};
use std::os::raw::{c_char, c_int};

type LoggingCallback = extern "C" fn(*const c_char);

#[no_mangle]
pub extern "C" fn start_arti(
    state_dir: *const c_char,
    cache_dir: *const c_char,
    socks_port: c_int,
    dns_port: c_int,
    log_fn: LoggingCallback,
) -> *mut c_char {
    let state_dir = unsafe { CStr::from_ptr(state_dir) }.to_string_lossy();
    let cache_dir = unsafe { CStr::from_ptr(cache_dir) }.to_string_lossy();

    let result = match start_arti_proxy(
        &cache_dir,
        &state_dir,
        None,
        None,
        0,
        socks_port as u16,
        dns_port as u16,
        move |buf: &[u8]| {
            let cstr = CString::new(buf.to_owned()).unwrap();
            (log_fn)(cstr.as_ptr());
        },
    ) {
        Ok(res) => format!("Output: {}", res),
        Err(e) => format!("Error: {}", e),
    };

    CString::new(result).unwrap().into_raw()
}
