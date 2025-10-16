package com.torrentclient.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.neomovies.torrentengine.models.FilePriority
import com.neomovies.torrentengine.models.TorrentFile
import com.torrentclient.app.R
import com.torrentclient.app.databinding.ItemTorrentFileBinding
import com.torrentclient.app.utils.FileUtils

/**
 * Adapter for displaying torrent files list with priority selection
 */
class TorrentFilesAdapter(
    private val onFilePriorityChanged: (TorrentFile, FilePriority) -> Unit
) : ListAdapter<TorrentFile, TorrentFilesAdapter.FileViewHolder>(FileDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemTorrentFileBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FileViewHolder(private val binding: ItemTorrentFileBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        fun bind(file: TorrentFile) {
            binding.apply {
                // File name
                fileName.text = file.getName()
                
                // File size
                fileSize.text = FileUtils.formatFileSize(file.size)
                
                // File type icon
                fileIcon.setImageResource(FileUtils.getFileTypeIcon(file.path))
                
                // Priority indicator
                priorityText.text = getPriorityText(file.priority)
                priorityText.setTextColor(getPriorityColor(file.priority))
                
                // Progress if available
                if (file.downloaded > 0) {
                    val progress = (file.downloaded.toFloat() / file.size * 100).toInt()
                    fileProgress.progress = progress
                    fileProgress.visibility = android.view.View.VISIBLE
                } else {
                    fileProgress.visibility = android.view.View.GONE
                }
                
                // Checkbox state
                fileCheckbox.isChecked = file.isSelected()
                fileCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    val newPriority = if (isChecked) FilePriority.NORMAL else FilePriority.DONT_DOWNLOAD
                    onFilePriorityChanged(file, newPriority)
                }
                
                // Click on the whole item to change priority
                root.setOnClickListener {
                    showPriorityDialog(file)
                }
            }
        }

        private fun getPriorityText(priority: FilePriority): String {
            return when (priority) {
                FilePriority.DONT_DOWNLOAD -> binding.root.context.getString(R.string.priority_dont_download)
                FilePriority.LOW -> binding.root.context.getString(R.string.priority_low)
                FilePriority.NORMAL -> binding.root.context.getString(R.string.priority_normal)
                FilePriority.HIGH -> binding.root.context.getString(R.string.priority_high)
                FilePriority.MAXIMUM -> binding.root.context.getString(R.string.priority_maximum)
            }
        }

        private fun getPriorityColor(priority: FilePriority): Int {
            val context = binding.root.context
            return when (priority) {
                FilePriority.DONT_DOWNLOAD -> context.getColor(R.color.state_paused)
                FilePriority.LOW -> context.getColor(R.color.text_secondary)
                FilePriority.NORMAL -> context.getColor(R.color.text_primary)
                FilePriority.HIGH -> context.getColor(R.color.accent)
                FilePriority.MAXIMUM -> context.getColor(R.color.state_downloading)
            }
        }

        private fun showPriorityDialog(file: TorrentFile) {
            val context = binding.root.context
            val priorities = arrayOf(
                context.getString(R.string.priority_dont_download),
                context.getString(R.string.priority_low),
                context.getString(R.string.priority_normal),
                context.getString(R.string.priority_high),
                context.getString(R.string.priority_maximum)
            )
            
            val currentPriorityIndex = when (file.priority) {
                FilePriority.DONT_DOWNLOAD -> 0
                FilePriority.LOW -> 1
                FilePriority.NORMAL -> 2
                FilePriority.HIGH -> 3
                FilePriority.MAXIMUM -> 4
            }
            
            MaterialAlertDialogBuilder(context)
                .setTitle(file.getName())
                .setSingleChoiceItems(priorities, currentPriorityIndex) { dialog, which ->
                    val newPriority = when (which) {
                        0 -> FilePriority.DONT_DOWNLOAD
                        1 -> FilePriority.LOW
                        2 -> FilePriority.NORMAL
                        3 -> FilePriority.HIGH
                        4 -> FilePriority.MAXIMUM
                        else -> FilePriority.NORMAL
                    }
                    onFilePriorityChanged(file, newPriority)
                    dialog.dismiss()
                }
                .setNegativeButton(context.getString(R.string.cancel), null)
                .show()
        }
    }
}

/**
 * DiffUtil callback for efficient list updates
 */
class FileDiffCallback : DiffUtil.ItemCallback<TorrentFile>() {
    override fun areItemsTheSame(oldItem: TorrentFile, newItem: TorrentFile): Boolean {
        return oldItem.index == newItem.index
    }

    override fun areContentsTheSame(oldItem: TorrentFile, newItem: TorrentFile): Boolean {
        return oldItem == newItem
    }
}