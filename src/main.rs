use os_mediamote::media_controller::MediaController;

fn main() {
    let mc = MediaController::new().unwrap();

    println!("{:?}", mc.media_is_playing());
    mc.media_play_pause().unwrap();
    std::thread::sleep(std::time::Duration::from_millis(100));
    println!("{:?}", mc.media_is_playing());
    println!("{:?}", mc.media_get_titile());
    println!("{:?}", mc.media_get_position());
    println!("{:?}", mc.media_get_duration());
}
