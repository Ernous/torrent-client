package com.torrentclient.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.neomovies.torrentengine.TorrentEngine

/**
 * Application class for initializing the torrent client
 */
class TorrentApplication : Application() {
    
    private val NOTIFICATION_CHANNEL_ID = "torrent_downloads"
    
    lateinit var torrentEngine: TorrentEngine
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize notification channel for foreground service
        createNotificationChannel()
        
        // Initialize torrent engine
        torrentEngine = TorrentEngine.getInstance(this)
        torrentEngine.startStatsUpdater()
    }
    
    /**
     * Create notification channel for torrent downloads
     * Required for Android 8.0+ (API 26+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
            }
            
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "torrent_downloads"
    }
}