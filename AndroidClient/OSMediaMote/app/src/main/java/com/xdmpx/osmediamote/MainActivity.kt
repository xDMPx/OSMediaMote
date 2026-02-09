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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xdmpx.osmediamote.ui.About.AboutUI
import com.xdmpx.osmediamote.ui.Main.IpInputScreen
import com.xdmpx.osmediamote.ui.Main.TopAppBar
import com.xdmpx.osmediamote.ui.MediaControlScreen.ArtIcon
import com.xdmpx.osmediamote.ui.MediaControlScreen.PositionSlider
import com.xdmpx.osmediamote.ui.theme.OSMediaMoteTheme
import com.xdmpx.osmediamote.utils.MediaControlRequester
import com.xdmpx.osmediamote.utils.Utils
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
    private lateinit var navController: NavController

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
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    this@MainActivity.navController = navController
                    NavHost(navController = navController, startDestination = "main") {
                        composable("main") {
                            Log.d("MainScreen", "Update")
                            Scaffold(
                                topBar = {
                                    TopAppBar() {
                                        navController.navigate("about")
                                    }
                                }, modifier = Modifier.fillMaxSize()
                            ) { innerPadding ->
                                val osMediaMoteState by osMediaMoteViewModel.osMediaMoteState.collectAsState()
                                LaunchedEffect(osMediaMoteViewModel) {
                                    cancelTimer()
                                }
                                IpInputScreen(
                                    osMediaMoteState.ipText,
                                    onClick = { ipText ->
                                        osMediaMoteViewModel.setDisplayProgressIndicator(true)
                                        osMediaMoteViewModel.setIp(ipText)
                                        MediaControlRequester.pingServer(
                                            ipText, this@MainActivity
                                        ) { success ->
                                            if (success) {
                                                this@MainActivity.navController.navigate("media_control_screen")
                                                scheduleTimer()
                                            } else {
                                                cancelTimer()
                                                osMediaMoteViewModel.setDisplayProgressIndicator(
                                                    false
                                                )
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    getString(R.string.error_connection_failed),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                osMediaMoteViewModel.setIpText(ipText)
                                            }
                                        }
                                        this@MainActivity.lifecycle.coroutineScope.launch { saveLastConnectedIPValue() }
                                    },
                                    osMediaMoteViewModel = osMediaMoteViewModel,
                                    enabled = !osMediaMoteState.displayProgressIndicator,
                                    modifier = Modifier
                                        .padding(innerPadding)
                                        .fillMaxSize()
                                )
                                if (osMediaMoteState.displayProgressIndicator) {
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
                            }
                        }
                        composable("media_control_screen") {
                            val osMediaMoteState by osMediaMoteViewModel.osMediaMoteState.collectAsState()
                            osMediaMoteViewModel.setDisplayProgressIndicator(false)
                            Scaffold(
                                topBar = {
                                    TopAppBar() {

                                    }
                                }, modifier = Modifier.fillMaxSize()
                            ) { innerPadding ->
                                osMediaMoteState.ip?.let {
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
                                            osMediaMoteViewModel = osMediaMoteViewModel,
                                            context = this@MainActivity,
                                            modifier = Modifier
                                                .fillMaxWidth(0.75f)
                                                .fillMaxHeight()
                                        )
                                    }
                                }
                            }
                        }
                        composable("about") {
                            AboutUI() {
                                navController.popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        cancelTimer()
    }

    override fun onResume() {
        super.onResume()
        osMediaMoteViewModel.osMediaMoteState.value.ip?.let {
            cancelTimer()
            scheduleTimer()
        }
    }

    private fun fetchData(ip: String) {
        MediaControlRequester.fetchTitle(ip, this@MainActivity, osMediaMoteViewModel)
        MediaControlRequester.fetchIsPlaying(ip, this@MainActivity, osMediaMoteViewModel)
        MediaControlRequester.fetchDuration(ip, this@MainActivity, osMediaMoteViewModel)
    }

    private suspend fun saveLastConnectedIPValue() {
        if (osMediaMoteViewModel.osMediaMoteState.value.ip.isNullOrBlank()) return
        val lastConnectedIPValueKey = stringPreferencesKey("last_connected_ip")
        this@MainActivity.dataStore.edit { preferences ->
            preferences[lastConnectedIPValueKey] = osMediaMoteViewModel.osMediaMoteState.value.ip!!
        }
    }

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

    private fun cancelTimer() {
        updateTimer?.cancel()
    }

    private fun scheduleTimer() {
        updateTimer?.cancel()
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
}
