use futures::executor::block_on;
use windows::Media;

#[derive(Debug)]
pub struct MediaControllerError {}

#[derive(Debug)]
pub struct MediaController {}

impl MediaController {
    pub fn new() -> Result<MediaController, MediaControllerError> {
        Ok(MediaController {})
    }

    pub fn media_pause(&self) -> Result<(), MediaControllerError> {
        block_on(self._media_pause())
    }

    async fn _media_pause(&self) -> Result<(), MediaControllerError> {
        let session_manager =
            Media::Control::GlobalSystemMediaTransportControlsSessionManager::RequestAsync()
                .unwrap()
                .await
                .unwrap();
        let session = session_manager.GetCurrentSession().unwrap();

        session.TryPauseAsync().unwrap().await.unwrap();
        Ok(())
    }

    pub async fn media_play(&self) -> Result<(), MediaControllerError> {
        block_on(self._media_play())
    }

    async fn _media_play(&self) -> Result<(), MediaControllerError> {
        let session_manager =
            Media::Control::GlobalSystemMediaTransportControlsSessionManager::RequestAsync()
                .unwrap()
                .await
                .unwrap();
        let session = session_manager.GetCurrentSession().unwrap();

        session.TryPlayAsync().unwrap().await.unwrap();
        Ok(())
    }

    pub fn media_play_pause(&self) -> Result<(), MediaControllerError> {
        block_on(self._media_play_pause())
    }

    async fn _media_play_pause(&self) -> Result<(), MediaControllerError> {
        let session_manager =
            Media::Control::GlobalSystemMediaTransportControlsSessionManager::RequestAsync()
                .unwrap()
                .await
                .unwrap();
        let session = session_manager.GetCurrentSession().unwrap();

        session.TryTogglePlayPauseAsync().unwrap().await.unwrap();
        Ok(())
    }

    pub fn media_get_title(&self) -> Result<String, MediaControllerError> {
        block_on(self._media_get_title())
    }

    async fn _media_get_title(&self) -> Result<String, MediaControllerError> {
        let session_manager =
            Media::Control::GlobalSystemMediaTransportControlsSessionManager::RequestAsync()
                .unwrap()
                .await
                .unwrap();
        let session = session_manager.GetCurrentSession().unwrap();
        let sesiion_media_properties = session.TryGetMediaPropertiesAsync().unwrap().await.unwrap();

        Ok(format!("{}", sesiion_media_properties.Title().unwrap()))
    }

    pub fn media_get_duration(&self) -> Result<f32, MediaControllerError> {
        block_on(self._media_get_duration())
    }

    async fn _media_get_duration(&self) -> Result<f32, MediaControllerError> {
        let session_manager =
            Media::Control::GlobalSystemMediaTransportControlsSessionManager::RequestAsync()
                .unwrap()
                .await
                .unwrap();
        let session = session_manager.GetCurrentSession().unwrap();
        let timeline_properties = session.GetTimelineProperties().unwrap();
        let end_time = timeline_properties.EndTime().unwrap();
        let duration = end_time.Duration as f64 / 10_000_000.0;

        Ok(duration as f32)
    }

    pub fn media_get_position(&self) -> Result<f32, MediaControllerError> {
        block_on(self._media_get_position())
    }

    async fn _media_get_position(&self) -> Result<f32, MediaControllerError> {
        let session_manager =
            Media::Control::GlobalSystemMediaTransportControlsSessionManager::RequestAsync()
                .unwrap()
                .await
                .unwrap();
        let session = session_manager.GetCurrentSession().unwrap();
        let timeline_properties = session.GetTimelineProperties().unwrap();
        let position = timeline_properties.Position().unwrap();
        let position = position.Duration as f64 / 10_000_000.0;

        Ok(position as f32)
    }

    pub fn media_is_playing(&self) -> Result<bool, MediaControllerError> {
        block_on(self._media_is_playing())
    }

    async fn _media_is_playing(&self) -> Result<bool, MediaControllerError> {
        let session_manager =
            Media::Control::GlobalSystemMediaTransportControlsSessionManager::RequestAsync()
                .unwrap()
                .await
                .unwrap();
        let session = session_manager.GetCurrentSession().unwrap();
        let playback_status = session.GetPlaybackInfo().unwrap().PlaybackStatus().unwrap();

        Ok(playback_status.0 == 4)
    }
}
