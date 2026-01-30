package com.xdmpx.osmediamote.ui

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.xdmpx.osmediamote.OSMediaMote

object MediaControlScreen {

    @Composable
    fun PositionSlider(position: Float, duration: Float) {
        Column {
            Slider(
                value = position, valueRange = 0.0f..duration, onValueChange = { })
        }
    }

    @Composable
    fun ArtIcon(ip: String, artHash: Int, osMediaMoteViewModel: OSMediaMote, modifier: Modifier = Modifier) {
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