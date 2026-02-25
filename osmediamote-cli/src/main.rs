use osmediamote_cli::{ProgramOption, print_help, process_args, reqwest_get, reqwest_put};

fn main() {
    let options = process_args()
        .map_err(|err| {
            match err {
                osmediamote_cli::OSMediaMoteError::InvalidOption(option) => {
                    eprintln!("Provided option {option} is invalid")
                }
                osmediamote_cli::OSMediaMoteError::InvalidOptionsStructure => {
                    eprintln!("Invalid input")
                }
                _ => panic!("{:?}", err),
            }
            print_help();
            std::process::exit(-1);
        })
        .unwrap();
    if options.contains(&ProgramOption::PrintHelp) {
        print_help();
        std::process::exit(-1);
    }

    let ip = options
        .iter()
        .find_map(|o| match o {
            ProgramOption::IP(ip) => Some(ip.to_owned()),
            _ => None,
        })
        .unwrap();

    for option in options {
        match option {
            ProgramOption::IP(_) => (),
            ProgramOption::Play => {
                let _ = reqwest_get(&format!("http://{ip}:65420/play")).unwrap();
            }
            ProgramOption::Pause => {
                let _ = reqwest_get(&format!("http://{ip}:65420/pause")).unwrap();
            }
            ProgramOption::PlayPause => {
                let _ = reqwest_get(&format!("http://{ip}:65420/play_pause")).unwrap();
            }
            ProgramOption::Next => {
                let _ = reqwest_get(&format!("http://{ip}:65420/play_next")).unwrap();
            }
            ProgramOption::Previous => {
                let _ = reqwest_get(&format!("http://{ip}:65420/play_prev")).unwrap();
            }
            ProgramOption::Status => {
                let res = reqwest_get(&format!("http://{ip}:65420/is_playing")).unwrap();
                println!("Playing: {}", res.text().unwrap());
            }
            ProgramOption::Position => {
                let res = reqwest_get(&format!("http://{ip}:65420/position")).unwrap();
                println!("{}", res.text().unwrap());
            }
            ProgramOption::SetPosition(position) => {
                let res = reqwest_put(&format!("http://{ip}:65420/position/{position}")).unwrap();
                println!("{}", res.text().unwrap());
            }
            ProgramOption::Metadata => {
                let res = reqwest_get(&format!("http://{ip}:65420/title")).unwrap();
                println!("Title: {}", res.text().unwrap());
                let res = reqwest_get(&format!("http://{ip}:65420/duration")).unwrap();
                println!("Duration: {}", res.text().unwrap());
                let res = reqwest_get(&format!("http://{ip}:65420/artist")).unwrap();
                println!("Artist: {}", res.text().unwrap());
            }
            ProgramOption::PrintHelp => unreachable!(),
        }
    }
}
