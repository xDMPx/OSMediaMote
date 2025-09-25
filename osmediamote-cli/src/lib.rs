#[inline(always)]
pub fn get_reqwest_client() -> reqwest::Result<reqwest::blocking::Client> {
    let user_agent: String = format!("{}/{}", env!("CARGO_PKG_NAME"), env!("CARGO_PKG_VERSION"));

    let reqwest_client = reqwest::blocking::Client::builder()
        .timeout(std::time::Duration::from_secs(1))
        .user_agent(user_agent)
        .build()?;

    Ok(reqwest_client)
}

#[inline(always)]
pub fn reqwest_get(url: &str) -> reqwest::Result<reqwest::blocking::Response> {
    let reqwest_client = get_reqwest_client()?;

    let request = reqwest_client.get(url).build()?;
    let response = reqwest_client.execute(request)?;

    Ok(response)
}

#[derive(Debug)]
pub enum OSMediaMoteError {
    InvalidOption(String),
    InvalidOptionsStructure,
    ReqwestError(reqwest::Error),
}

#[derive(PartialEq)]
pub enum ProgramOption {
    IP(String),
    Play,
    Pause,
    PlayPause,
    Next,
    Previous,
    Status,
    Position,
    Metadata,
}

pub fn process_args() -> Result<Vec<ProgramOption>, OSMediaMoteError> {
    let mut options = vec![];
    let mut args: Vec<String> = std::env::args().skip(1).collect();
    args.reverse();

    let last_arg = args
        .pop()
        .ok_or(OSMediaMoteError::InvalidOptionsStructure)?;
    let ip = last_arg;
    if ip.chars().filter(|&c| c == '.').count() != 3 {
        return Err(OSMediaMoteError::InvalidOptionsStructure);
    }
    options.push(ProgramOption::IP(ip));

    for arg in args {
        let arg = match arg.as_str() {
            "play" => Ok(ProgramOption::Play),
            "pause" => Ok(ProgramOption::Pause),
            "play-pause" => Ok(ProgramOption::PlayPause),
            "next" => Ok(ProgramOption::Next),
            "previous" => Ok(ProgramOption::Previous),
            "status" => Ok(ProgramOption::Status),
            "position" => Ok(ProgramOption::Position),
            "metadata" => Ok(ProgramOption::Metadata),
            _ => Err(OSMediaMoteError::InvalidOption(arg)),
        };
        options.push(arg?);
    }

    if options.len() != 2 {
        return Err(OSMediaMoteError::InvalidOptionsStructure);
    }

    Ok(options)
}
