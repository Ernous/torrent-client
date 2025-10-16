package com.torrentclient.app.ui

import androidx.lifecycle.*
import com.neomovies.torrentengine.TorrentEngine
import com.neomovies.torrentengine.models.FilePriority
import com.neomovies.torrentengine.models.TorrentInfo
import com.torrentclient.app.TorrentApplication
import com.torrentclient.app.utils.FileUtils
import kotlinx.coroutines.launch

/**
 * ViewModel for torrent details screen
 */
class TorrentDetailsViewModel(
    private val torrentEngine: TorrentEngine,
    private val infoHash: String
) : ViewModel() {
    
    // Live data for the specific torrent
    val torrent: LiveData<TorrentInfo?> = torrentEngine.getAllTorrentsFlow()
        .asLiveData()
        .map { torrents -> torrents.find { it.infoHash == infoHash } }
    
    /**
     * Set priority for a specific file
     */
    fun setFilePriority(fileIndex: Int, priority: FilePriority) {
        viewModelScope.launch {
            try {
                torrentEngine.setFilePriority(infoHash, fileIndex, priority)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Set priority for all files
     */
    fun setAllFilesPriority(priority: FilePriority) {
        viewModelScope.launch {
            try {
                val torrentInfo = torrent.value ?: return@launch
                val priorities = mutableMapOf<Int, FilePriority>()
                
                torrentInfo.files.forEachIndexed { index, _ ->
                    priorities[index] = priority
                }
                
                torrentEngine.setFilePriorities(infoHash, priorities)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Select only video files with high priority, ignore others
     */
    fun selectVideoFiles() {
        viewModelScope.launch {
            try {
                val torrentInfo = torrent.value ?: return@launch
                val priorities = mutableMapOf<Int, FilePriority>()
                
                torrentInfo.files.forEachIndexed { index, file ->
                    priorities[index] = if (FileUtils.isVideoFile(file.path)) {
                        FilePriority.HIGH
                    } else {
                        FilePriority.DONT_DOWNLOAD
                    }
                }
                
                torrentEngine.setFilePriorities(infoHash, priorities)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * ViewModelFactory for dependency injection
     */
    class Factory(
        private val application: TorrentApplication,
        private val infoHash: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TorrentDetailsViewModel::class.java)) {
                return TorrentDetailsViewModel(application.torrentEngine, infoHash) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}