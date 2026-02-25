use mpris::PlayerFinder;

#[derive(Debug)]
pub struct MediaControllerError {
    finding_error: Option<mpris::FindingError>,
    dbus_error: Option<mpris::DBusError>,
}

#[derive(Debug)]
pub struct MediaController {
    player: mpris::PlayerFinder,
}

impl MediaController {
    pub fn new() -> Result<MediaController, MediaControllerError> {
        Ok(MediaController {
            player: PlayerFinder::new().map_err(|e| MediaControllerError {
                dbus_error: Some(e),
                finding_error: None,
            })?,
        })
    }

    pub fn media_pause(&self) -> Result<(), MediaControllerError> {
        let player: mpris::Player =
            self.player
                .find_active()
                .map_err(|e| MediaControllerError {
                    finding_error: Some(e),
                    dbus_error: None,
                })?;

        player.pause().map_err(|e| MediaControllerError {
            dbus_error: Some(e),
            finding_error: None,
        })?;

        Ok(())
    }

    pub fn media_play(&self) -> Result<(), MediaControllerError> {
        let player: mpris::Player =
            self.player
                .find_active()
                .map_err(|e| MediaControllerError {
                    finding_error: Some(e),
                    dbus_error: None,
                })?;

        player.play().map_err(|e| MediaControllerError {
            dbus_error: Some(e),
            finding_error: None,
        })?;

        Ok(())
    }

    pub fn media_play_pause(&self) -> Result<(), MediaControllerError> {
        let player: mpris::Player =
            self.player
                .find_active()
                .map_err(|e| MediaControllerError {
                    finding_error: Some(e),
                    dbus_error: None,
                })?;

        player.play_pause().map_err(|e| MediaControllerError {
            dbus_error: Some(e),
            finding_error: None,
        })?;

        Ok(())
    }

    pub fn media_get_title(&self) -> Result<String, MediaControllerError> {
        let player: mpris::Player =
            self.player
                .find_active()
                .map_err(|e| MediaControllerError {
                    finding_error: Some(e),
                    dbus_error: None,
                })?;

        let title = player
            .get_metadata()
            .map_err(|e| MediaControllerError {
                dbus_error: Some(e),
                finding_error: None,
            })?
            .title()
            .unwrap_or("")
            .to_owned();

        Ok(title)
    }

    pub fn media_get_artist(&self) -> Result<String, MediaControllerError> {
        let player: mpris::Player =
            self.player
                .find_active()
                .map_err(|e| MediaControllerError {
                    finding_error: Some(e),
                    dbus_error: None,
                })?;

        let artists = player
            .get_metadata()
            .map_err(|e| MediaControllerError {
                dbus_error: Some(e),
                finding_error: None,
            })?
            .artists()
            .map(|x| x.join(", "))
            .unwrap_or("".to_string());

        Ok(artists)
    }

    pub fn media_get_art(&self) -> Result<Vec<u8>, MediaControllerError> {
        let player: mpris::Player =
            self.player
                .find_active()
                .map_err(|e| MediaControllerError {
                    finding_error: Some(e),
                    dbus_error: None,
                })?;

        let art_url = player
            .get_metadata()
            .map_err(|e| MediaControllerError {
                dbus_error: Some(e),
                finding_error: None,
            })?
            .art_url()
            .unwrap_or("")
            .to_owned();

        println!("Art URL: {art_url}");

        if art_url.starts_with("file://") {
            let path = art_url.strip_prefix("file://").unwrap();
            let path = urlencoding::decode(path).unwrap().into_owned();
            let mut image_file = std::fs::File::open(path).unwrap();
            let mut data = vec![];
            std::io::Read::read_to_end(&mut image_file, &mut data).unwrap();

            Ok(data)
        } else {
            Ok(art_url.as_bytes().to_owned())
        }
    }

    pub fn media_get_duration(&self) -> Result<f32, MediaControllerError> {
        let player: mpris::Player =
            self.player
                .find_active()
                .map_err(|e| MediaControllerError {
                    finding_error: Some(e),
                    dbus_error: None,
                })?;

        let duration = player
            .get_metadata()
            .map_err(|e| MediaControllerError {
                dbus_error: Some(e),
                finding_error: None,
            })?
            .length()
            .unwrap_or(std::time::Duration::new(0, 0));

        Ok(duration.as_secs_f32())
    }

    pub fn media_get_position(&self) -> Result<f32, MediaControllerError> {
        let player: mpris::Player =
            self.player
                .find_active()
                .map_err(|e| MediaControllerError {
                    finding_error: Some(e),
                    dbus_error: None,
                })?;

        let position = player.get_position().map_err(|e| MediaControllerError {
            dbus_error: Some(e),
            finding_error: None,
        })?;

        Ok(position.as_secs_f32())
    }

    pub fn media_set_position(&self, position: u64) -> Result<(), MediaControllerError> {
        let player: mpris::Player =
            self.player
                .find_active()
                .map_err(|e| MediaControllerError {
                    finding_error: Some(e),
                    dbus_error: None,
                })?;

        let metadata = player.get_metadata().map_err(|e| MediaControllerError {
            dbus_error: Some(e),
            finding_error: None,
        })?;

        //TODO: corectly handle lack of track_id
        let track_id = metadata.track_id().unwrap();

        player
            .set_position(track_id, &std::time::Duration::from_secs(position))
            .map_err(|e| MediaControllerError {
                dbus_error: Some(e),
                finding_error: None,
            })?;

        Ok(())
    }

    pub fn media_is_playing(&self) -> Result<bool, MediaControllerError> {
        let player: mpris::Player =
            self.player
                .find_active()
                .map_err(|e| MediaControllerError {
                    finding_error: Some(e),
                    dbus_error: None,
                })?;

        let ps = player
            .get_playback_status()
            .map_err(|e| MediaControllerError {
                dbus_error: Some(e),
                finding_error: None,
            })?;

        if ps == mpris::PlaybackStatus::Playing {
            Ok(true)
        } else {
            Ok(false)
        }
    }

    pub fn media_play_next(&self) -> Result<(), MediaControllerError> {
        let player: mpris::Player =
            self.player
                .find_active()
                .map_err(|e| MediaControllerError {
                    finding_error: Some(e),
                    dbus_error: None,
                })?;

        let metadata = player.get_metadata().map_err(|e| MediaControllerError {
            dbus_error: Some(e),
            finding_error: None,
        })?;

        let duration = metadata
            .length()
            .unwrap_or(std::time::Duration::new(0, 0))
            .as_secs();

        //TODO: corectly handle lack of track_id
        let track_id = metadata.track_id().unwrap();

        player
            .set_position(track_id, &(std::time::Duration::from_secs(duration - 2)))
            .map_err(|e| MediaControllerError {
                finding_error: None,
                dbus_error: Some(e),
            })?;

        Ok(())
    }

    pub fn media_play_next2(&self) -> Result<(), MediaControllerError> {
        let player: mpris::Player =
            self.player
                .find_active()
                .map_err(|e| MediaControllerError {
                    finding_error: Some(e),
                    dbus_error: None,
                })?;

        player.checked_next().map_err(|e| MediaControllerError {
            finding_error: None,
            dbus_error: Some(e),
        })?;

        Ok(())
    }

    pub fn media_play_prev(&self) -> Result<(), MediaControllerError> {
        let player: mpris::Player =
            self.player
                .find_active()
                .map_err(|e| MediaControllerError {
                    finding_error: Some(e),
                    dbus_error: None,
                })?;

        player
            .checked_previous()
            .map_err(|e| MediaControllerError {
                finding_error: None,
                dbus_error: Some(e),
            })?;

        Ok(())
    }
}
