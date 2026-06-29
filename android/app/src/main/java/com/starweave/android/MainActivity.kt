package com.starweave.android

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.starweave.android.navigation.AppNavigation
import com.starweave.android.service.MusicService
import com.starweave.android.ui.theme.StarWeaveTheme

class MainActivity : ComponentActivity() {
    private var musicService: MusicService? = null
    private var bound = false
    private var musicPlayingState: MutableState<Boolean>? = null
    private val playbackListener: (Boolean) -> Unit = { playing ->
        musicPlayingState?.value = playing
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            bound = true
            musicService?.addPlaybackListener(playbackListener)
            // Auto-play music
            musicService?.play()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService?.removePlaybackListener(playbackListener)
            musicService = null
            bound = false
            musicPlayingState?.value = false
        }
    }

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or not, music still works */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Bind music service
        Intent(this, MusicService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        setContent {
            var musicPlaying by remember { mutableStateOf(false) }
            DisposableEffect(Unit) {
                musicPlayingState = object : MutableState<Boolean> {
                    override var value: Boolean
                        get() = musicPlaying
                        set(value) {
                            musicPlaying = value
                        }

                    override fun component1(): Boolean = value
                    override fun component2(): (Boolean) -> Unit = { value = it }
                }
                musicService?.addPlaybackListener(playbackListener)
                onDispose {
                    musicService?.removePlaybackListener(playbackListener)
                    musicPlayingState = null
                }
            }

            StarWeaveTheme {
                AppNavigation(
                    musicPlaying = musicPlaying,
                    onToggleMusic = { musicService?.toggle() }
                )
            }
        }
    }

    override fun onDestroy() {
        if (bound) {
            musicService?.removePlaybackListener(playbackListener)
            unbindService(connection)
            bound = false
        }
        super.onDestroy()
    }
}
