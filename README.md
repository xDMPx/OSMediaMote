# OSMediaMote

Remotly control system media playback and retrive playback metadata via MPRIS(Linux) and (GlobalSystemMediaTransportControls) Windows.

## Clients

- [osmediamote-cli](osmediamote-cli) - cli client inspired by playerctl
- [android][AndroidClient] - android app

## Building

To build, clone this repository and run:
```sh
cargo build --release
```

## Installation

### Linux

Locally:
```sh
CARGO_INSTALL_ROOT=~/.local cargo install --path=.
```

### Windows

Build the binary as described in [Building](#Building) section and use the generated executable in `target/release`.

## Usage

Just run the generated executable or use:
```
cargo run --release
```

And the server will start on port `65420`, then connect via one of the clients to control or retrieve information about media playback.

## Endpoints

| Endpoint      | Method | Description                               |
| ------------- | ------ | ----------------------------------------- |
| `/pause`      | GET    | Pause playback.                           |
| `/play`       | GET    | Start playback.                           |
| `/play_pause` | GET    | Toggle play/pause.                        |
| `/play_next`  | GET    | Skip to next track.                       |
| `/play_prev`  | GET    | Skip to previous track.                   |
| `/title`      | GET    | Get current track title.                  |
| `/artist`     | GET    | Get current track artist(s).              |
| `/art`        | GET    | Get current track artwork (image bytes).  |
| `/duration`   | GET    | Get track duration in seconds.            |
| `/position`   | GET    | Get current playback position in seconds. |
| `/is_playing` | GET    | Check if media is currently playing.      |
| `/ping`       | GET    | Health check, returns 200 OK.             |

## License

This project is licensed under [MIT](LICENSE) License.
