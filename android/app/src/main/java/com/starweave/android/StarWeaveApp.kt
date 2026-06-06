package com.starweave.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class StarWeaveApp : Application() {
    companion object {
        const val CHANNEL_MUSIC = "starweave_music"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_MUSIC,
                "背景音乐",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "StarWeave 背景音乐播放"
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }
}
