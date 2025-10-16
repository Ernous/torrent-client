package com.torrentclient.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.torrentclient.app.databinding.ActivityMainBinding
import com.torrentclient.app.ui.AddTorrentDialogFragment
import com.torrentclient.app.ui.TorrentDetailsActivity
import com.torrentclient.app.ui.TorrentListAdapter
import com.torrentclient.app.ui.TorrentListViewModel
import com.torrentclient.app.utils.FileUtils

class MainActivity : AppCompatActivity(), AddTorrentDialogFragment.AddTorrentListener {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: TorrentListViewModel
    private lateinit var adapter: TorrentListAdapter
    
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            Toast.makeText(this, R.string.error_storage_permission, Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        
        // Request storage permissions
        requestStoragePermissions()
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(
            this,
            TorrentListViewModel.Factory(application as TorrentApplication)
        )[TorrentListViewModel::class.java]
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Setup FAB
        binding.fabAddTorrent.setOnClickListener {
            showAddTorrentDialog()
        }
        
        // Observe torrents list
        viewModel.torrents.observe(this) { torrents ->
            adapter.submitList(torrents)
            
            // Show empty state if no torrents
            binding.emptyStateText.visibility = if (torrents.isEmpty()) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
        }
        
        // Handle magnet link from intent
        handleMagnetIntent(intent)
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleMagnetIntent(it) }
    }
    
    private fun requestStoragePermissions() {
        val permissions = mutableListOf<String>()
        
        // For Android 11+ we need MANAGE_EXTERNAL_STORAGE permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // We'll handle this in the settings or show instructions to user
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        
        if (permissions.isNotEmpty()) {
            storagePermissionLauncher.launch(permissions.toTypedArray())
        }
    }
    
    private fun setupRecyclerView() {
        adapter = TorrentListAdapter(
            onTorrentClick = { torrent ->
                // Open torrent details
                val intent = Intent(this, TorrentDetailsActivity::class.java).apply {
                    putExtra("TORRENT_INFO_HASH", torrent.infoHash)
                }
                startActivity(intent)
            },
            onTorrentLongClick = { torrent ->
                // Show context menu
                showTorrentContextMenu(torrent.infoHash)
            }
        )
        
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }
    
    private fun showAddTorrentDialog() {
        val dialog = AddTorrentDialogFragment()
        dialog.show(supportFragmentManager, "add_torrent")
    }
    
    private fun showTorrentContextMenu(infoHash: String) {
        val torrent = viewModel.torrents.value?.find { it.infoHash == infoHash } ?: return
        
        val items = arrayOf(
            if (torrent.isActive()) getString(R.string.pause) else getString(R.string.resume),
            getString(R.string.remove),
            getString(R.string.remove_with_files)
        )
        
        MaterialAlertDialogBuilder(this)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> {
                        if (torrent.isActive()) {
                            viewModel.pauseTorrent(infoHash)
                        } else {
                            viewModel.resumeTorrent(infoHash)
                        }
                    }
                    1 -> viewModel.removeTorrent(infoHash, false)
                    2 -> viewModel.removeTorrent(infoHash, true)
                }
            }
            .show()
    }
    
    private fun handleMagnetIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
            val magnetUri = intent.data.toString()
            if (FileUtils.isValidMagnetUri(magnetUri)) {
                // Show add torrent dialog with pre-filled magnet URI
                val dialog = AddTorrentDialogFragment.newInstance(magnetUri)
                dialog.show(supportFragmentManager, "add_torrent_from_magnet")
            } else {
                Toast.makeText(this, R.string.error_invalid_magnet, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // Implementation of AddTorrentDialogFragment.AddTorrentListener
    override fun onTorrentAdded(magnetUri: String, savePath: String) {
        viewModel.addTorrent(magnetUri, savePath)
    }
}