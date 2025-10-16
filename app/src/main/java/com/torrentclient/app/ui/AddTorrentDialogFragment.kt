package com.torrentclient.app.ui

import android.app.Dialog
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.torrentclient.app.R
import com.torrentclient.app.databinding.DialogAddTorrentBinding
import com.torrentclient.app.utils.FileUtils

/**
 * Dialog fragment for adding new torrent
 */
class AddTorrentDialogFragment : DialogFragment() {
    
    private lateinit var binding: DialogAddTorrentBinding
    private var magnetUri: String? = null
    
    interface AddTorrentListener {
        fun onTorrentAdded(magnetUri: String, savePath: String)
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddTorrentBinding.inflate(layoutInflater)
        
        // Pre-fill magnet URI if provided
        magnetUri = arguments?.getString(ARG_MAGNET_URI)
        if (magnetUri != null) {
            binding.editMagnetUri.setText(magnetUri)
        } else {
            // Try to get magnet link from clipboard
            tryGetMagnetFromClipboard()
        }
        
        // Set default save path
        val defaultPath = FileUtils.getDefaultDownloadsDir(requireContext()).absolutePath
        binding.editSavePath.setText(defaultPath)
        
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_torrent_title)
            .setView(binding.root)
            .setPositiveButton(R.string.add_torrent_btn) { _, _ ->
                addTorrent()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }
    
    private fun tryGetMagnetFromClipboard() {
        try {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = clipboard.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val clipText = clipData.getItemAt(0).text?.toString() ?: return
                if (FileUtils.isValidMagnetUri(clipText)) {
                    binding.editMagnetUri.setText(clipText)
                }
            }
        } catch (e: Exception) {
            // Ignore clipboard errors
        }
    }
    
    private fun addTorrent() {
        val magnetUri = binding.editMagnetUri.text.toString().trim()
        val savePath = binding.editSavePath.text.toString().trim()
        
        // Validate inputs
        if (magnetUri.isEmpty()) {
            Toast.makeText(requireContext(), "Введите magnet-ссылку", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!FileUtils.isValidMagnetUri(magnetUri)) {
            Toast.makeText(requireContext(), R.string.error_invalid_magnet, Toast.LENGTH_SHORT).show()
            return
        }
        
        if (savePath.isEmpty()) {
            Toast.makeText(requireContext(), "Выберите путь для сохранения", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Create directory if it doesn't exist
        val saveDir = java.io.File(savePath)
        if (!FileUtils.ensureDirectoryExists(saveDir)) {
            Toast.makeText(requireContext(), "Не удалось создать директорию", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Call listener
        val listener = activity as? AddTorrentListener
        listener?.onTorrentAdded(magnetUri, savePath)
        
        dismiss()
    }
    
    companion object {
        private const val ARG_MAGNET_URI = "magnet_uri"
        
        fun newInstance(magnetUri: String? = null): AddTorrentDialogFragment {
            val fragment = AddTorrentDialogFragment()
            if (magnetUri != null) {
                val args = Bundle()
                args.putString(ARG_MAGNET_URI, magnetUri)
                fragment.arguments = args
            }
            return fragment
        }
    }
}