# osmediamote-cli

Playerctl inspired cli client for OSMediaMote server.

## Building

To build, clone this repository and run in the osmediamote-cli directory:
```sh
cargo build --release
```

## Installation

### Linux

Locally, in the osmediamote-cli directory run:
```sh
CARGO_INSTALL_ROOT=~/.local cargo install --path=.
```

### Windows

Build the binary as described in [Building](#Building) section and use the generated executable in `osmediamote-cli/target/release`.

## Usage

```
Usage: osmediamote-cli IP COMMAND
       osmediamote-cli --help
Commands:
	 play
	 pause
	 play-pause
	 next
	 previous
	 status
	 position
     position=[OFFSET]
	 metadata
```
