use actix_web::{get, middleware::Logger, web, App, HttpResponse, HttpServer, Responder};
use os_mediamote::media_controller;
use std::{fs::File, io::Read};

struct AppState {
    mc: media_controller::MediaController,
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    env_logger::init_from_env(env_logger::Env::default().default_filter_or("info"));

    HttpServer::new(|| {
        App::new()
            .app_data(web::Data::new(AppState {
                mc: media_controller::MediaController::new().unwrap(),
            }))
            .wrap(Logger::default())
            .service(pause)
            .service(play)
            .service(play_pause)
            .service(play_next)
            .service(play_prev)
            .service(title)
            .service(art)
            .service(duration)
            .service(position)
            .service(is_playing)
    })
    .bind(("0.0.0.0", 65420))?
    .run()
    .await
}

#[get("/pause")]
async fn pause(data: web::Data<AppState>) -> impl Responder {
    data.mc.media_pause().unwrap();
    HttpResponse::Ok()
}

#[get("/play")]
async fn play(data: web::Data<AppState>) -> impl Responder {
    data.mc.media_play().unwrap();
    HttpResponse::Ok()
}

#[get("/play_pause")]
async fn play_pause(data: web::Data<AppState>) -> impl Responder {
    data.mc.media_play_pause().unwrap();
    HttpResponse::Ok()
}

#[get("/play_next")]
async fn play_next(data: web::Data<AppState>) -> impl Responder {
    data.mc.media_play_next().unwrap();
    HttpResponse::Ok()
}

#[get("/play_prev")]
async fn play_prev(data: web::Data<AppState>) -> impl Responder {
    data.mc.media_play_prev().unwrap();
    HttpResponse::Ok()
}

#[get("/title")]
async fn title(data: web::Data<AppState>) -> impl Responder {
    let title = data.mc.media_get_title().unwrap();
    HttpResponse::Ok()
        .content_type("text/plain; charset=utf-8")
        .body(title)
}

#[cfg(target_os = "linux")]
#[get("/art")]
async fn art(data: web::Data<AppState>) -> impl Responder {
    let art = data.mc.media_get_art().unwrap();
    println!("Art: {}", art);
    if art.starts_with("file://") {
        let path = art.strip_prefix("file://").unwrap();
        let path = urlencoding::decode(path).unwrap().into_owned();
        println!("Art path: {}", path);
        let mut image_file = File::open(path).unwrap();
        let mut data = vec![];
        image_file.read_to_end(&mut data).unwrap();
        HttpResponse::Ok().body(data)
    } else {
        HttpResponse::Ok()
            .content_type("text/plain; charset=utf-8")
            .body(art)
    }
}

#[get("/duration")]
async fn duration(data: web::Data<AppState>) -> impl Responder {
    let duration = data.mc.media_get_duration().unwrap();
    HttpResponse::Ok()
        .content_type("text/plain; charset=utf-8")
        .body(format!("{duration}"))
}

#[get("/position")]
async fn position(data: web::Data<AppState>) -> impl Responder {
    let position = data.mc.media_get_position().unwrap();
    HttpResponse::Ok()
        .content_type("text/plain; charset=utf-8")
        .body(format!("{position}"))
}

#[get("/is_playing")]
async fn is_playing(data: web::Data<AppState>) -> impl Responder {
    let is_playing = data.mc.media_is_playing().unwrap();
    HttpResponse::Ok()
        .content_type("text/plain; charset=utf-8")
        .body(format!("{is_playing}"))
}
