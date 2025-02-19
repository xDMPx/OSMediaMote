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

    pub fn media_get_titile(&self) -> Result<String, MediaControllerError> {
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
}
