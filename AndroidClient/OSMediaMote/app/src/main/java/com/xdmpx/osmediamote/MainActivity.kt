package com.xdmpx.osmediamote

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.coroutineScope
import coil.compose.AsyncImage
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.xdmpx.osmediamote.ui.theme.OSMediaMoteTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "osmediamote_store")

class MainActivity : ComponentActivity() {
    private lateinit var volleyQueue: RequestQueue
    private var updateTimer: Timer? = null

    private var ip: MutableState<String?> = mutableStateOf(null)
    private var ipText: MutableState<String> = mutableStateOf("")
    private var title: MutableState<String> = mutableStateOf("")
    private var duration: MutableState<String> = mutableStateOf("")
    private var position: MutableState<String> = mutableStateOf("")
    private var isPlaying: MutableState<Boolean> = mutableStateOf(false)
    private var artHash: MutableState<Int> = mutableIntStateOf(0)
    private var drawFallbackIcon: MutableState<Boolean> = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        volleyQueue = Volley.newRequestQueue(this)

        val lastConnectedIPValueKey = stringPreferencesKey("last_connected_ip")
        val lastConnectedIPValue: Flow<String> = this.dataStore.data.catch {}.map { preferences ->
            preferences[lastConnectedIPValueKey] ?: ""
        }

        this.lifecycle.coroutineScope.launch {
            val value = lastConnectedIPValue.first()
            Log.i("MainActivity", "LastConnectedIP -> $value")
            ipText.value = value
        }

        enableEdgeToEdge()
        setContent {
            OSMediaMoteTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (ip.value == null) {
                        IpInputScreen(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                        )
                    } else {
                        ip.value?.let {
                            scheduleTimer()
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                            ) {
                                MediaControlScreen(
                                    it, Modifier
                                        .fillMaxWidth(0.75f)
                                        .fillMaxHeight()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        updateTimer?.cancel()
    }

    override fun onResume() {
        super.onResume()
        ip.value?.let {
            updateTimer?.cancel()
            scheduleTimer()
        }
    }

    private fun fetchData(ip: String) {
        fetchTitle(ip)
        fetchIsPlaying(ip)
        fetchDuration(ip)
    }

    private suspend fun saveLastConnectedIPValue() {
        if (ip.value.isNullOrBlank()) return
        val lastConnectedIPValueKey = stringPreferencesKey("last_connected_ip")
        this@MainActivity.dataStore.edit { preferences ->
            preferences[lastConnectedIPValueKey] = ip.value!!
        }
    }

    @Composable
    private fun IpInputScreen(modifier: Modifier = Modifier) {
        var text by remember { mutableStateOf("") }
        if (ipText.value.isNotBlank() && text.isBlank()) {
            text = ipText.value
            ipText.value = ""
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {
            TextField(value = text, onValueChange = { text = it }, label = { Text("IP") })
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                {
                    ip.value = text
                    this@MainActivity.lifecycle.coroutineScope.launch { saveLastConnectedIPValue() }
                }, modifier = Modifier.fillMaxWidth(0.5f)
            ) { Text("Confirm") }
        }

    }

    @Composable
    fun ArtIcon(modifier: Modifier = Modifier) {
        // Hash value used to circumvent the caching
        val url = "http://${ip.value}:65420/art?hash=${artHash.value}"
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
                    drawFallbackIcon.value = true
                    //if (it.result.throwable.toString().contains("403")) {}
                }, modifier = Modifier.fillMaxSize()
            )
        }
    }

    @Composable
    private fun MediaControlScreen(ip: String, modifier: Modifier = Modifier) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {
            val iconModifier = Modifier.size(75.dp)

            Box(
                contentAlignment = Alignment.Center, modifier = Modifier.height(200.dp)
            ) {
                if (!drawFallbackIcon.value) {
                    ArtIcon()
                } else {
                    Icon(
                        painterResource(R.drawable.rounded_music_video_24),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Text(title.value)
            val positionInHMS =
                position.value.toFloatOrNull()?.let { secsToHMS(it.toLong()) }.orEmpty()
            val durationInHMS =
                duration.value.toFloatOrNull()?.let { secsToHMS(it.toLong()) }.orEmpty()
            Column {
                position.value.toFloatOrNull()?.let { pos ->
                    duration.value.toFloatOrNull()?.let {
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
                IconButton(onClick = { requestPlayPrev(ip) }, modifier = iconModifier) {
                    Icon(
                        painterResource(R.drawable.rounded_skip_previous_24),
                        contentDescription = null,
                        modifier = iconModifier
                    )
                }
                IconButton(onClick = { requestPlayPause(ip) }, modifier = iconModifier) {
                    if (isPlaying.value) {
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
                IconButton(onClick = { requestPlayNext(ip) }, modifier = iconModifier) {
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

    private fun scheduleTimer() {
        updateTimer = Timer()
        updateTimer?.schedule(
            object : TimerTask() {
                override fun run() {
                    ip.value?.let {
                        fetchData(it)
                    }
                }
            }, 0, 500
        )
    }

    private fun fetchTitle(ip: String) {
        val url = "http://${ip}:65420/title"

        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            if (title.value != response.toString()) {
                artHash.value += 1
                drawFallbackIcon.value = false
            }
            title.value = response.toString()
        }, { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    private fun fetchDuration(ip: String) {
        val url = "http://${ip}:65420/duration"

        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            duration.value = response.toString()
        }, { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    private fun fetchPosition(ip: String) {
        val url = "http://${ip}:65420/position"

        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            position.value = response.toString()
        }, { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    private fun fetchIsPlaying(ip: String) {
        val url = "http://${ip}:65420/is_playing"

        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            isPlaying.value = response.toString() == "true"
            if (isPlaying.value) {
                fetchPosition(ip)
            }
        }, { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    private fun requestPause(ip: String) {
        val url = "http://${ip}:65420/pause"

        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { _ -> },
            { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    private fun requestPlay(ip: String) {
        val url = "http://${ip}:65420/play"

        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { _ -> },
            { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    private fun requestPlayPause(ip: String) {
        val url = "http://${ip}:65420/play_pause"

        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { _ -> },
            { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    private fun requestPlayNext(ip: String) {
        val url = "http://${ip}:65420/play_next"

        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { _ -> },
            { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    private fun requestPlayPrev(ip: String) {
        val url = "http://${ip}:65420/play_prev"

        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { _ -> },
            { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    private fun secsToHMS(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds - h * 3600) / 60
        val s = seconds - h * 3600 - m * 60

        return String.format(null, "%02d:%02d:%02d", h, m, s)
    }

}
