package com.torrentclient.app.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.neomovies.torrentengine.models.FilePriority
import com.neomovies.torrentengine.models.TorrentFile
import com.torrentclient.app.R
import com.torrentclient.app.TorrentApplication
import com.torrentclient.app.databinding.ActivityTorrentDetailsBinding
import com.torrentclient.app.utils.FileUtils

/**
 * Activity for displaying torrent details and managing files
 */
class TorrentDetailsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityTorrentDetailsBinding
    private lateinit var viewModel: TorrentDetailsViewModel
    private lateinit var filesAdapter: TorrentFilesAdapter
    private var infoHash: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityTorrentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Get torrent info hash from intent
        infoHash = intent.getStringExtra("TORRENT_INFO_HASH")
        if (infoHash == null) {
            Toast.makeText(this, R.string.error_torrent_not_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(
            this,
            TorrentDetailsViewModel.Factory(application as TorrentApplication, infoHash!!)
        )[TorrentDetailsViewModel::class.java]
        
        setupRecyclerView()
        setupObservers()
        setupButtons()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    private fun setupRecyclerView() {
        filesAdapter = TorrentFilesAdapter { file, newPriority ->
            viewModel.setFilePriority(file.index, newPriority)
        }
        
        binding.filesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@TorrentDetailsActivity)
            adapter = filesAdapter
        }
    }
    
    private fun setupObservers() {
        // Observe torrent info
        viewModel.torrent.observe(this) { torrent ->
            if (torrent != null) {
                // Update toolbar title
                supportActionBar?.title = torrent.name
                
                // Update torrent info
                binding.apply {
                    // Progress
                    progressBar.progress = (torrent.progress * 100).toInt()
                    progressText.text = getString(R.string.progress_format, torrent.progress * 100)
                    
                    // Size
                    sizeText.text = "${FileUtils.formatFileSize(torrent.downloadedSize)} / " +
                            FileUtils.formatFileSize(torrent.totalSize)
                    
                    // Speed
                    if (torrent.isActive()) {
                        speedText.text = "↓ ${FileUtils.formatSpeed(torrent.downloadSpeed)} " +
                                "↑ ${FileUtils.formatSpeed(torrent.uploadSpeed)}"
                    } else {
                        speedText.text = "Inactive"
                    }
                    
                    // ETA
                    if (torrent.downloadSpeed > 0) {
                        etaText.text = getString(R.string.eta_format, FileUtils.formatEta(torrent.getEta()))
                    } else {
                        etaText.text = "∞"
                    }
                    
                    // Peers
                    peersText.text = getString(R.string.peers_format, torrent.numPeers, torrent.numSeeds)
                    
                    // Share ratio
                    ratioText.text = getString(R.string.ratio_format, torrent.getShareRatio())
                    
                    // Files selected
                    selectedFilesText.text = "${torrent.getSelectedFilesCount()} из ${torrent.files.size} файлов"
                }
                
                // Update files list
                filesAdapter.submitList(torrent.files)
            }
        }
    }
    
    private fun setupButtons() {
        // Select all files
        binding.btnSelectAll.setOnClickListener {
            showPrioritySelectionDialog(true)
        }
        
        // Deselect all files  
        binding.btnDeselectAll.setOnClickListener {
            viewModel.setAllFilesPriority(FilePriority.DONT_DOWNLOAD)
        }
        
        // Select only video files
        binding.btnSelectVideos.setOnClickListener {
            viewModel.selectVideoFiles()
        }
    }
    
    private fun showPrioritySelectionDialog(selectAll: Boolean) {
        val priorities = arrayOf(
            getString(R.string.priority_normal),
            getString(R.string.priority_high),
            getString(R.string.priority_maximum)
        )
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Выберите приоритет")
            .setItems(priorities) { _, which ->
                val priority = when (which) {
                    0 -> FilePriority.NORMAL
                    1 -> FilePriority.HIGH
                    2 -> FilePriority.MAXIMUM
                    else -> FilePriority.NORMAL
                }
                
                if (selectAll) {
                    viewModel.setAllFilesPriority(priority)
                }
            }
            .show()
    }
}