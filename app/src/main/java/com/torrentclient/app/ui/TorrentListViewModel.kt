package com.torrentclient.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.neomovies.torrentengine.TorrentEngine
import com.torrentclient.app.TorrentApplication
import com.torrentclient.app.utils.FileUtils
import kotlinx.coroutines.launch

/**
 * ViewModel for the torrent list screen
 */
class TorrentListViewModel(private val torrentEngine: TorrentEngine) : ViewModel() {
    
    // Expose torrents as LiveData for UI observation
    val torrents = torrentEngine.getAllTorrentsFlow().asLiveData()
    
    /**
     * Add new torrent from magnet URI
     */
    fun addTorrent(magnetUri: String, savePath: String) {
        viewModelScope.launch {
            try {
                torrentEngine.addTorrent(magnetUri, savePath)
            } catch (e: Exception) {
                // Error handling can be added here (via LiveData or EventBus)
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Pause torrent
     */
    fun pauseTorrent(infoHash: String) {
        viewModelScope.launch {
            try {
                torrentEngine.pauseTorrent(infoHash)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Resume torrent
     */
    fun resumeTorrent(infoHash: String) {
        viewModelScope.launch {
            try {
                torrentEngine.resumeTorrent(infoHash)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Remove torrent
     */
    fun removeTorrent(infoHash: String, deleteFiles: Boolean = false) {
        viewModelScope.launch {
            try {
                torrentEngine.removeTorrent(infoHash, deleteFiles)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * ViewModelFactory for dependency injection
     */
    class Factory(private val application: TorrentApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TorrentListViewModel::class.java)) {
                return TorrentListViewModel(application.torrentEngine) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}