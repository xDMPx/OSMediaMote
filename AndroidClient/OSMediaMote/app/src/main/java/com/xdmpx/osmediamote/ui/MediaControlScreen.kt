package com.xdmpx.osmediamote.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.xdmpx.osmediamote.OSMediaMote
import com.xdmpx.osmediamote.R
import com.xdmpx.osmediamote.utils.MediaControlRequester
import com.xdmpx.osmediamote.utils.Utils

object MediaControlScreen {

    @Composable
    fun MediaControlScreen(
        ip: String,
        artHash: Int,
        title: String,
        position: String,
        duration: String,
        drawFallbackIcon: Boolean,
        isPlaying: Boolean,
        osMediaMoteViewModel: OSMediaMote,
        context: Context,
        modifier: Modifier = Modifier
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {
            val iconModifier = Modifier.size(75.dp)

            Box(
                contentAlignment = Alignment.Center, modifier = Modifier.height(200.dp)
            ) {
                if (!drawFallbackIcon) {
                    ArtIcon(ip, artHash, osMediaMoteViewModel)
                } else {
                    Icon(
                        painterResource(R.drawable.rounded_music_video_24),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Text(title)
            val positionInHMS =
                position.toFloatOrNull()?.let { Utils.secsToHMS(it.toLong()) }.orEmpty()
            val durationInHMS =
                duration.toFloatOrNull()?.let { Utils.secsToHMS(it.toLong()) }.orEmpty()
            Column {
                position.toFloatOrNull()?.let { pos ->
                    duration.toFloatOrNull()?.let {
                        PositionSlider(pos, it)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = positionInHMS, fontSize = 12.sp)
                    Text(
                        durationInHMS,
                        fontSize = 12.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Row {
                IconButton(
                    onClick = { MediaControlRequester.requestPlayPrev(ip, context) },
                    modifier = iconModifier
                ) {
                    Icon(
                        painterResource(R.drawable.rounded_skip_previous_24),
                        contentDescription = null,
                        modifier = iconModifier
                    )
                }
                IconButton(
                    onClick = { MediaControlRequester.requestPlayPause(ip, context) },
                    modifier = iconModifier
                ) {
                    if (isPlaying) {
                        Icon(
                            painterResource(R.drawable.round_pause_24),
                            contentDescription = null,
                            modifier = iconModifier,
                        )
                    } else {
                        Icon(
                            painterResource(R.drawable.round_play_arrow_24),
                            contentDescription = null,
                            modifier = iconModifier,
                        )
                    }
                }
                IconButton(
                    onClick = { MediaControlRequester.requestPlayNext(ip, context) },
                    modifier = iconModifier
                ) {
                    Icon(
                        painterResource(R.drawable.rounded_skip_next_24),
                        contentDescription = null,
                        modifier = iconModifier
                    )
                }
            }

        }

    }

    @Composable
    fun PositionSlider(position: Float, duration: Float) {
        Column {
            Slider(
                value = position, valueRange = 0.0f..duration, onValueChange = { })
        }
    }

    @Composable
    fun ArtIcon(
        ip: String, artHash: Int, osMediaMoteViewModel: OSMediaMote, modifier: Modifier = Modifier
    ) {
        // Hash value used to circumvent the caching
        val url = "http://${ip}:65420/art?hash=${artHash}"
        Log.d("ArtIcon", url)
        Box(
            contentAlignment = Alignment.Center, modifier = modifier
                .fillMaxHeight()
                .padding(5.dp)
        ) {
            AsyncImage(
                model = url, contentDescription = null, onError = {
                    Log.e(
                        "AsyncImage", "$url Failed: ${it.result.throwable}"
                    )
                    osMediaMoteViewModel.setDrawFallbackIcon(true)
                    //if (it.result.throwable.toString().contains("403")) {}
                }, modifier = Modifier.fillMaxSize()
            )
        }
    }
}