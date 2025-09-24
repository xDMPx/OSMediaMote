package com.xdmpx.osmediamote

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration


data class OSMediaMoteState(
    val ip: String? = null,
    val ipText: String = "",
    val title: String = "",
    val duration: String = "",
    val position: String = "",
    val isPlaying: Boolean = false,
    val artHash: Int = 0,
    val drawFallbackIcon: Boolean = false,
    val pingState: Int = 0
)

class OSMediaMote : ViewModel() {
    private val _osMediaMoteState = MutableStateFlow(OSMediaMoteState())
    val osMediaMoteState: StateFlow<OSMediaMoteState> = _osMediaMoteState.asStateFlow()

    fun setIp(ip: String) {
        _osMediaMoteState.value.let {
            _osMediaMoteState.value = it.copy(ip = ip)
        }
    }

    fun setIpText(ipText: String) {
        _osMediaMoteState.value.let {
            _osMediaMoteState.value = it.copy(ipText = ipText)
        }
    }

    fun setTitle(title: String) {
        _osMediaMoteState.value.let {
            _osMediaMoteState.value = it.copy(title = title)
        }
    }

    fun setPosition(position: String) {
        _osMediaMoteState.value.let {
            _osMediaMoteState.value = it.copy(position = position)
        }
    }

    fun setDuration(duration: String) {
        _osMediaMoteState.value.let {
            _osMediaMoteState.value = it.copy(duration = duration)
        }
    }

    fun setIsPlaying(isPlaying: Boolean) {
        _osMediaMoteState.value.let {
            _osMediaMoteState.value = it.copy(isPlaying = isPlaying)
        }
    }

    fun setPingState(state: Int) {
        _osMediaMoteState.value.let {
            _osMediaMoteState.value = it.copy(pingState = state)
        }
    }

    fun setDrawFallbackIcon(draw: Boolean) {
        _osMediaMoteState.value.let {
            _osMediaMoteState.value = it.copy(drawFallbackIcon = draw)
        }
    }

    fun incrementArtHash() {
        _osMediaMoteState.value.let {
            _osMediaMoteState.value = it.copy(artHash = it.artHash + 1)
        }
    }

}