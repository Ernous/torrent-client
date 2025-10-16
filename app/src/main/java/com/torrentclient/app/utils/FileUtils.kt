package com.torrentclient.app.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.text.DecimalFormat

/**
 * Utility functions for file operations and formatting
 */
object FileUtils {

    /**
     * Format file size to human readable format
     */
    fun formatFileSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0) return "0 B"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(sizeInBytes.toDouble()) / Math.log10(1024.0)).toInt()
        
        return DecimalFormat("#,##0.#").format(
            sizeInBytes / Math.pow(1024.0, digitGroups.toDouble())
        ) + " " + units[digitGroups]
    }

    /**
     * Format transfer speed to human readable format
     */
    fun formatSpeed(speedInBytes: Int): String {
        if (speedInBytes <= 0) return "0 B/s"
        return "${formatFileSize(speedInBytes.toLong())}/s"
    }

    /**
     * Format ETA (Estimated Time of Arrival) to human readable format
     */
    fun formatEta(etaInSeconds: Long): String {
        if (etaInSeconds <= 0 || etaInSeconds == Long.MAX_VALUE) return "∞"
        
        val hours = etaInSeconds / 3600
        val minutes = (etaInSeconds % 3600) / 60
        val seconds = etaInSeconds % 60
        
        return when {
            hours > 0 -> String.format("%dh %02dm", hours, minutes)
            minutes > 0 -> String.format("%dm %02ds", minutes, seconds)
            else -> String.format("%ds", seconds)
        }
    }

    /**
     * Get default downloads directory
     */
    fun getDefaultDownloadsDir(context: Context): File {
        // Try to use external storage downloads directory
        val externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return if (externalDir?.exists() == true) {
            File(externalDir, "TorrentClient")
        } else {
            // Fallback to app-specific external storage
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "TorrentClient")
        }
    }

    /**
     * Create directory if it doesn't exist
     */
    fun ensureDirectoryExists(dir: File): Boolean {
        return if (!dir.exists()) {
            dir.mkdirs()
        } else {
            dir.isDirectory
        }
    }

    /**
     * Check if external storage is writable
     */
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /**
     * Get file extension from filename
     */
    fun getFileExtension(filename: String): String {
        val lastDot = filename.lastIndexOf('.')
        return if (lastDot >= 0) {
            filename.substring(lastDot + 1).lowercase()
        } else {
            ""
        }
    }

    /**
     * Check if file is a video file based on extension
     */
    fun isVideoFile(filename: String): Boolean {
        val videoExtensions = setOf(
            "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", 
            "m4v", "mpg", "mpeg", "3gp", "ts", "m2ts"
        )
        return getFileExtension(filename) in videoExtensions
    }

    /**
     * Check if file is an audio file based on extension
     */
    fun isAudioFile(filename: String): Boolean {
        val audioExtensions = setOf(
            "mp3", "flac", "wav", "aac", "ogg", "wma", "m4a", 
            "opus", "ape", "ac3", "dts"
        )
        return getFileExtension(filename) in audioExtensions
    }

    /**
     * Check if file is an archive based on extension
     */
    fun isArchiveFile(filename: String): Boolean {
        val archiveExtensions = setOf(
            "zip", "rar", "7z", "tar", "gz", "bz2", "xz", "lzma"
        )
        return getFileExtension(filename) in archiveExtensions
    }

    /**
     * Get appropriate icon resource ID based on file type
     */
    fun getFileTypeIcon(filename: String): Int {
        return when {
            isVideoFile(filename) -> android.R.drawable.ic_media_play
            isAudioFile(filename) -> android.R.drawable.ic_lock_silent_mode
            isArchiveFile(filename) -> android.R.drawable.ic_menu_save
            else -> android.R.drawable.ic_menu_info_details
        }
    }

    /**
     * Validate magnet URI
     */
    fun isValidMagnetUri(uri: String): Boolean {
        return uri.isNotBlank() && 
               uri.startsWith("magnet:?", ignoreCase = true) && 
               uri.contains("xt=urn:btih:", ignoreCase = true)
    }

    /**
     * Extract torrent name from magnet URI
     */
    fun extractTorrentNameFromMagnet(magnetUri: String): String? {
        val dnIndex = magnetUri.indexOf("&dn=", ignoreCase = true)
        if (dnIndex == -1) return null
        
        val start = dnIndex + 4
        val end = magnetUri.indexOf("&", start).let { if (it == -1) magnetUri.length else it }
        
        return try {
            java.net.URLDecoder.decode(magnetUri.substring(start, end), "UTF-8")
        } catch (e: Exception) {
            null
        }
    }
}