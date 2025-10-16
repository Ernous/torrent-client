package com.torrentclient.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neomovies.torrentengine.models.TorrentInfo
import com.neomovies.torrentengine.models.TorrentState
import com.torrentclient.app.R
import com.torrentclient.app.databinding.ItemTorrentBinding
import com.torrentclient.app.utils.FileUtils

/**
 * RecyclerView adapter for torrent list
 */
class TorrentListAdapter(
    private val onTorrentClick: (TorrentInfo) -> Unit,
    private val onTorrentLongClick: (TorrentInfo) -> Unit
) : ListAdapter<TorrentInfo, TorrentListAdapter.TorrentViewHolder>(TorrentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TorrentViewHolder {
        val binding = ItemTorrentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TorrentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TorrentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TorrentViewHolder(private val binding: ItemTorrentBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        fun bind(torrent: TorrentInfo) {
            binding.apply {
                // Torrent name
                torrentName.text = torrent.name

                // Progress
                progressBar.progress = (torrent.progress * 100).toInt()
                progressText.text = root.context.getString(
                    R.string.progress_format, torrent.progress * 100
                )

                // State and speed
                stateText.text = getStateText(torrent.state)
                stateText.setTextColor(getStateColor(torrent.state))
                
                if (torrent.isActive()) {
                    speedText.text = root.context.getString(
                        R.string.speed_format, 
                        FileUtils.formatSpeed(torrent.downloadSpeed)
                    )
                    speedText.visibility = android.view.View.VISIBLE
                } else {
                    speedText.visibility = android.view.View.GONE
                }

                // Size info
                sizeText.text = "${FileUtils.formatFileSize(torrent.downloadedSize)} / " +
                        FileUtils.formatFileSize(torrent.totalSize)

                // ETA
                if (torrent.state == TorrentState.DOWNLOADING && torrent.downloadSpeed > 0) {
                    etaText.text = root.context.getString(
                        R.string.eta_format, 
                        FileUtils.formatEta(torrent.getEta())
                    )
                    etaText.visibility = android.view.View.VISIBLE
                } else {
                    etaText.visibility = android.view.View.GONE
                }

                // Peers
                if (torrent.isActive()) {
                    peersText.text = root.context.getString(
                        R.string.peers_format, 
                        torrent.numPeers, 
                        torrent.numSeeds
                    )
                    peersText.visibility = android.view.View.VISIBLE
                } else {
                    peersText.visibility = android.view.View.GONE
                }

                // Click listeners
                root.setOnClickListener { onTorrentClick(torrent) }
                root.setOnLongClickListener { 
                    onTorrentLongClick(torrent)
                    true
                }
            }
        }

        private fun getStateText(state: TorrentState): String {
            return when (state) {
                TorrentState.STOPPED -> root.context.getString(R.string.state_stopped)
                TorrentState.QUEUED -> root.context.getString(R.string.state_queued)
                TorrentState.METADATA_DOWNLOADING -> root.context.getString(R.string.state_metadata_downloading)
                TorrentState.CHECKING -> root.context.getString(R.string.state_checking)
                TorrentState.DOWNLOADING -> root.context.getString(R.string.state_downloading)
                TorrentState.SEEDING -> root.context.getString(R.string.state_seeding)
                TorrentState.FINISHED -> root.context.getString(R.string.state_finished)
                TorrentState.ERROR -> root.context.getString(R.string.state_error)
            }
        }

        private fun getStateColor(state: TorrentState): Int {
            val context = binding.root.context
            return when (state) {
                TorrentState.DOWNLOADING -> ContextCompat.getColor(context, R.color.state_downloading)
                TorrentState.SEEDING -> ContextCompat.getColor(context, R.color.state_seeding)
                TorrentState.FINISHED -> ContextCompat.getColor(context, R.color.state_finished)
                TorrentState.ERROR -> ContextCompat.getColor(context, R.color.state_error)
                else -> ContextCompat.getColor(context, R.color.state_paused)
            }
        }
    }
}

/**
 * DiffUtil callback for efficient list updates
 */
class TorrentDiffCallback : DiffUtil.ItemCallback<TorrentInfo>() {
    override fun areItemsTheSame(oldItem: TorrentInfo, newItem: TorrentInfo): Boolean {
        return oldItem.infoHash == newItem.infoHash
    }

    override fun areContentsTheSame(oldItem: TorrentInfo, newItem: TorrentInfo): Boolean {
        return oldItem == newItem
    }
}