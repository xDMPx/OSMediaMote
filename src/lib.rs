#[cfg_attr(not(target_os = "windows"), path = "media_controller.rs")]
#[cfg_attr(target_os = "windows", path = "media_controller_win.rs")]
pub mod media_controller;
