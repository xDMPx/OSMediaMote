package com.xdmpx.osmediamote

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    private val osMediaMoteViewModel by viewModels<OSMediaMote>()

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
            osMediaMoteViewModel.setIpText(value)
        }

        enableEdgeToEdge()
        setContent {
            OSMediaMoteTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val osMediaMoteState by osMediaMoteViewModel.osMediaMoteState.collectAsState()
                    if (osMediaMoteState.ip == null || osMediaMoteState.pingState != 2) {
                        if (osMediaMoteState.pingState == 0) {
                            IpInputScreen(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                            )
                        }
                        if (osMediaMoteState.pingState == 1) {
                            IpInputScreen(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                            )
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = Color(0x7FA8A8A8))
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.width(64.dp),
                                    color = MaterialTheme.colorScheme.secondary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                )
                            }
                        }
                    } else {
                        osMediaMoteState.ip?.let {
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
        osMediaMoteViewModel.osMediaMoteState.value.ip.let {
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
        if (osMediaMoteViewModel.osMediaMoteState.value.ip.isNullOrBlank()) return
        val lastConnectedIPValueKey = stringPreferencesKey("last_connected_ip")
        this@MainActivity.dataStore.edit { preferences ->
            preferences[lastConnectedIPValueKey] = osMediaMoteViewModel.osMediaMoteState.value.ip!!
        }
    }

    @Composable
    private fun IpInputScreen(modifier: Modifier = Modifier) {
        val osMediaMoteState by osMediaMoteViewModel.osMediaMoteState.collectAsState()
        var text by remember { mutableStateOf("") }
        if (osMediaMoteState.ipText.isNotBlank() && text.isBlank()) {
            text = osMediaMoteState.ipText
            osMediaMoteViewModel.setIpText("")
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
                    osMediaMoteViewModel.setIp(text)
                    osMediaMoteViewModel.setPingState(1)
                    pingServer(text)
                    this@MainActivity.lifecycle.coroutineScope.launch { saveLastConnectedIPValue() }
                }, modifier = Modifier.fillMaxWidth(0.5f)
            ) { Text("Confirm") }
        }

    }

    @Composable
    fun ArtIcon(modifier: Modifier = Modifier) {
        val osMediaMoteState by osMediaMoteViewModel.osMediaMoteState.collectAsState()
        // Hash value used to circumvent the caching
        val url = "http://${osMediaMoteState.ip}:65420/art?hash=${osMediaMoteState.artHash}"
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

    @Composable
    private fun MediaControlScreen(ip: String, modifier: Modifier = Modifier) {
        val osMediaMoteState by osMediaMoteViewModel.osMediaMoteState.collectAsState()
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {
            val iconModifier = Modifier.size(75.dp)

            Box(
                contentAlignment = Alignment.Center, modifier = Modifier.height(200.dp)
            ) {
                if (!osMediaMoteState.drawFallbackIcon) {
                    ArtIcon()
                } else {
                    Icon(
                        painterResource(R.drawable.rounded_music_video_24),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Text(osMediaMoteState.title)
            val positionInHMS =
                osMediaMoteState.position.toFloatOrNull()?.let { secsToHMS(it.toLong()) }.orEmpty()
            val durationInHMS =
                osMediaMoteState.duration.toFloatOrNull()?.let { secsToHMS(it.toLong()) }.orEmpty()
            Column {
                osMediaMoteState.position.toFloatOrNull()?.let { pos ->
                    osMediaMoteState.duration.toFloatOrNull()?.let {
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
                    if (osMediaMoteState.isPlaying) {
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
                    osMediaMoteViewModel.osMediaMoteState.value.ip?.let {
                        fetchData(it)
                    }
                }
            }, 0, 500
        )
    }

    private fun pingServer(ip: String) {
        val url = "http://${ip}:65420/ping"

        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            osMediaMoteViewModel.setPingState(2)
        }, { err ->
            Log.e("VolleyError:", "$url -> $err")
            osMediaMoteViewModel.setPingState(0)
            Toast.makeText(
                this@MainActivity, "Connection failed", Toast.LENGTH_SHORT
            ).show()
            osMediaMoteViewModel.setIpText(ip)
        })

        volleyQueue.add(stringRequest)
    }

    private fun fetchTitle(ip: String) {
        val url = "http://${ip}:65420/title"

        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            if (osMediaMoteViewModel.osMediaMoteState.value.title != response.toString()) {
                osMediaMoteViewModel.incrementArtHash()
                osMediaMoteViewModel.setDrawFallbackIcon(false)
            }
            osMediaMoteViewModel.setTitle(response.toString())
        }, { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    private fun fetchDuration(ip: String) {
        val url = "http://${ip}:65420/duration"

        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            osMediaMoteViewModel.setDuration(response.toString())
        }, { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    private fun fetchPosition(ip: String) {
        val url = "http://${ip}:65420/position"

        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            osMediaMoteViewModel.setPosition(response.toString())
        }, { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    private fun fetchIsPlaying(ip: String) {
        val url = "http://${ip}:65420/is_playing"

        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            osMediaMoteViewModel.setIsPlaying(response.toString() == "true")
            if (osMediaMoteViewModel.osMediaMoteState.value.isPlaying) {
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
