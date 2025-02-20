package com.xdmpx.osmediamote

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.xdmpx.osmediamote.ui.theme.OSMediaMoteTheme
import java.util.Timer
import java.util.TimerTask

class MainActivity : ComponentActivity() {
    private lateinit var volleyQueue: RequestQueue
    private var updateTimer: Timer? = null

    private var ip: MutableState<String?> = mutableStateOf(null)
    private var title: MutableState<String> = mutableStateOf("")
    private var duration: MutableState<String> = mutableStateOf("")
    private var position: MutableState<String> = mutableStateOf("")
    private var isPlaying: MutableState<Boolean> = mutableStateOf(false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        volleyQueue = Volley.newRequestQueue(this)

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

    @Composable
    private fun IpInputScreen(modifier: Modifier = Modifier) {
        var text by remember { mutableStateOf("") }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {
            TextField(value = text, onValueChange = { text = it }, label = { Text("IP") })
            Spacer(modifier = Modifier.height(10.dp))
            Button({ ip.value = text }, modifier = Modifier.fillMaxWidth(0.5f)) { Text("Confirm") }
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

            Text(title.value)
            Text("${position.value} / ${duration.value}")
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

        val stringRequest = StringRequest(Request.Method.GET,
            url,
            { _ -> },
            { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    private fun requestPlay(ip: String) {
        val url = "http://${ip}:65420/play"

        val stringRequest = StringRequest(Request.Method.GET,
            url,
            { _ -> },
            { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

    private fun requestPlayPause(ip: String) {
        val url = "http://${ip}:65420/play_pause"

        val stringRequest = StringRequest(Request.Method.GET,
            url,
            { _ -> },
            { err -> Log.e("VolleyError:", "$url -> $err") })

        volleyQueue.add(stringRequest)
    }

}