use actix_web::{get, web, App, HttpResponse, HttpServer, Responder};
use os_mediamote::media_controller;

struct AppState {
    mc: media_controller::MediaController,
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    HttpServer::new(|| {
        App::new()
            .app_data(web::Data::new(AppState {
                mc: media_controller::MediaController::new().unwrap(),
            }))
            .service(pause)
            .service(play)
            .service(play_pause)
            .service(title)
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

#[get("/title")]
async fn title(data: web::Data<AppState>) -> impl Responder {
    let title = data.mc.media_get_title().unwrap();
    HttpResponse::Ok().body(title)
}

#[get("/duration")]
async fn duration(data: web::Data<AppState>) -> impl Responder {
    let duration = data.mc.media_get_duration().unwrap();
    HttpResponse::Ok().body(format!("{duration}"))
}

#[get("/position")]
async fn position(data: web::Data<AppState>) -> impl Responder {
    let position = data.mc.media_get_position().unwrap();
    HttpResponse::Ok().body(format!("{position}"))
}

#[get("/is_playing")]
async fn is_playing(data: web::Data<AppState>) -> impl Responder {
    let is_playing = data.mc.media_is_playing().unwrap();
    HttpResponse::Ok().body(format!("{is_playing}"))
}
