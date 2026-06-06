package com.starweave.android.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.starweave.android.MainActivity
import com.starweave.android.R
import com.starweave.android.StarWeaveApp

class MusicService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private val binder = MusicBinder()

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music)?.apply {
            isLooping = true
            setVolume(0.35f, 0.35f)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> play()
            ACTION_PAUSE -> pause()
            ACTION_TOGGLE -> toggle()
        }
        return START_STICKY
    }

    fun play() {
        mediaPlayer?.start()
        startForeground(1, buildNotification(true))
    }

    fun pause() {
        mediaPlayer?.pause()
        startForeground(1, buildNotification(false))
    }

    fun toggle() {
        if (mediaPlayer?.isPlaying == true) pause() else play()
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true

    private fun buildNotification(playing: Boolean): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val toggleIntent = PendingIntent.getService(
            this, 1,
            Intent(this, MusicService::class.java).apply { action = ACTION_TOGGLE },
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, StarWeaveApp.CHANNEL_MUSIC)
            .setContentTitle("StarWeave")
            .setContentText(if (playing) "♪ 背景音乐播放中" else "背景音乐已暂停")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(openIntent)
            .addAction(
                if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (playing) "暂停" else "播放",
                toggleIntent
            )
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    companion object {
        const val ACTION_PLAY = "com.starweave.PLAY"
        const val ACTION_PAUSE = "com.starweave.PAUSE"
        const val ACTION_TOGGLE = "com.starweave.TOGGLE"
    }
}
