package com.xdmpx.osmediamote

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.xdmpx.osmediamote.ui.About.AboutUI
import com.xdmpx.osmediamote.ui.Main.IpInputScreen
import com.xdmpx.osmediamote.ui.Main.TopAppBar
import com.xdmpx.osmediamote.ui.MediaControlScreen.ArtIcon
import com.xdmpx.osmediamote.ui.MediaControlScreen.PositionSlider
import com.xdmpx.osmediamote.ui.theme.OSMediaMoteTheme
import com.xdmpx.osmediamote.utils.VolleyRequestQueue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "osmediamote_store")

class MainActivity : ComponentActivity() {
    private var updateTimer: Timer? = null
    private val osMediaMoteViewModel by viewModels<OSMediaMote>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                val osMediaMoteState by osMediaMoteViewModel.osMediaMoteState.collectAsState()
                Log.d("MainScreen", "Update")
                if (!osMediaMoteState.aboutScreen) {
                    Scaffold(
                        topBar = { TopAppBar() { osMediaMoteViewModel.setAboutScreen(true) } },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        val osMediaMoteState by osMediaMoteViewModel.osMediaMoteState.collectAsState()
                        if (osMediaMoteState.ip == null || osMediaMoteState.pingState != 2) {
                            if (osMediaMoteState.pingState == 0) {
                                IpInputScreen(
                                    osMediaMoteState.ipText,
                                    onClick = { ipText ->
                                        osMediaMoteViewModel.setIp(ipText)
                                        osMediaMoteViewModel.setPingState(1)
                                        pingServer(ipText, this@MainActivity)
                                        this@MainActivity.lifecycle.coroutineScope.launch { saveLastConnectedIPValue() }
                                    },
                                    osMediaMoteViewModel = osMediaMoteViewModel,
                                    modifier = Modifier
                                        .padding(innerPadding)
                                        .fillMaxSize()
                                )
                            }
                            if (osMediaMoteState.pingState == 1) {
                                IpInputScreen(
                                    osMediaMoteState.ipText,
                                    onClick = { ipText ->
                                        osMediaMoteViewModel.setIp(ipText)
                                        osMediaMoteViewModel.setPingState(1)
                                        pingServer(ipText, this@MainActivity)
                                        this@MainActivity.lifecycle.coroutineScope.launch { saveLastConnectedIPValue() }
                                    },
                                    osMediaMoteViewModel = osMediaMoteViewModel,
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
                                        ip = it,
                                        title = osMediaMoteState.title,
                                        position = osMediaMoteState.position,
                                        duration = osMediaMoteState.duration,
                                        artHash = osMediaMoteState.artHash,
                                        isPlaying = osMediaMoteState.isPlaying,
                                        drawFallbackIcon = osMediaMoteState.drawFallbackIcon,
                                        modifier = Modifier
                                            .fillMaxWidth(0.75f)
                                            .fillMaxHeight()
                                    )
                                }
                            }
                        }
                    }
                } else {
                    AboutUI() { osMediaMoteViewModel.setAboutScreen(false) }
                    this.onBackPressedDispatcher.addCallback {
                        if (osMediaMoteViewModel.osMediaMoteState.value.aboutScreen) {
                            this@addCallback.remove()
                            osMediaMoteViewModel.setAboutScreen(false)
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
        fetchTitle(ip, this@MainActivity)
        fetchIsPlaying(ip, this@MainActivity)
        fetchDuration(ip, this@MainActivity)
    }

    private suspend fun saveLastConnectedIPValue() {
        if (osMediaMoteViewModel.osMediaMoteState.value.ip.isNullOrBlank()) return
        val lastConnectedIPValueKey = stringPreferencesKey("last_connected_ip")
        this@MainActivity.dataStore.edit { preferences ->
            preferences[lastConnectedIPValueKey] = osMediaMoteViewModel.osMediaMoteState.value.ip!!
        }
    }


    @Composable
    private fun MediaControlScreen(
        ip: String,
        artHash: Int,
        title: String,
        position: String,
        duration: String,
        drawFallbackIcon: Boolean,
        isPlaying: Boolean,
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
            val positionInHMS = position.toFloatOrNull()?.let { secsToHMS(it.toLong()) }.orEmpty()
            val durationInHMS = duration.toFloatOrNull()?.let { secsToHMS(it.toLong()) }.orEmpty()
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
                    onClick = { requestPlayPrev(ip, this@MainActivity) }, modifier = iconModifier
                ) {
                    Icon(
                        painterResource(R.drawable.rounded_skip_previous_24),
                        contentDescription = null,
                        modifier = iconModifier
                    )
                }
                IconButton(
                    onClick = { requestPlayPause(ip, this@MainActivity) }, modifier = iconModifier
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
                    onClick = { requestPlayNext(ip, this@MainActivity) }, modifier = iconModifier
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

    private fun pingServer(ip: String, context: Context) {
        val volleyQueue: RequestQueue = VolleyRequestQueue.getInstance(context)
        val url = "http://${ip}:65420/ping"

        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            osMediaMoteViewModel.setPingState(2)
        }, { err ->
            Log.e("VolleyError:", "$url -> $err")
            osMediaMoteViewModel.setPingState(0)
            Toast.makeText(
                this@MainActivity, getString(R.string.error_connection_failed), Toast.LENGTH_SHORT
            ).show()
            osMediaMoteViewModel.setIpText(ip)
        })

        volleyQueue.add(stringRequest)
    }

    private fun fetchTitle(ip: String, context: Context) {
        val volleyQueue: RequestQueue = VolleyRequestQueue.getInstance(context)
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

    private fun fetchDuration(ip: String, context: Context) {
        val volleyQueue: RequestQueue = VolleyRequestQueue.getInstance(context)
        val url = "http://${ip}:65420/duration"

        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            osMediaMoteViewModel.setDuration(response.toString())
        }, { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    private fun fetchPosition(ip: String, context: Context) {
        val volleyQueue: RequestQueue = VolleyRequestQueue.getInstance(context)
        val url = "http://${ip}:65420/position"

        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            osMediaMoteViewModel.setPosition(response.toString())
        }, { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    private fun fetchIsPlaying(ip: String, context: Context) {
        val volleyQueue: RequestQueue = VolleyRequestQueue.getInstance(context)
        val url = "http://${ip}:65420/is_playing"

        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            osMediaMoteViewModel.setIsPlaying(response.toString() == "true")
            if (osMediaMoteViewModel.osMediaMoteState.value.isPlaying) {
                fetchPosition(ip, context)
            }
        }, { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    private fun requestPause(ip: String, context: Context) {
        val volleyQueue: RequestQueue = VolleyRequestQueue.getInstance(context)
        val url = "http://${ip}:65420/pause"

        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { _ -> },
            { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    private fun requestPlay(ip: String, context: Context) {
        val volleyQueue: RequestQueue = VolleyRequestQueue.getInstance(context)
        val url = "http://${ip}:65420/play"

        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { _ -> },
            { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    private fun requestPlayPause(ip: String, context: Context) {
        val volleyQueue: RequestQueue = VolleyRequestQueue.getInstance(context)
        val url = "http://${ip}:65420/play_pause"

        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { _ -> },
            { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    private fun requestPlayNext(ip: String, context: Context) {
        val volleyQueue: RequestQueue = VolleyRequestQueue.getInstance(context)
        val url = "http://${ip}:65420/play_next"

        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { _ -> },
            { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    private fun requestPlayPrev(ip: String, context: Context) {
        val volleyQueue: RequestQueue = VolleyRequestQueue.getInstance(context)
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