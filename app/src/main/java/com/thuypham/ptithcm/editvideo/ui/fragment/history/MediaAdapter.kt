package com.thuypham.ptithcm.editvideo.ui.fragment.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.archko.editvideo.db.VideoProgress
import com.thuypham.ptithcm.editvideo.databinding.ItemMediaFileBinding

class MediaAdapter(
    private val onItemPlay: ((item: VideoProgress) -> Unit)? = null,
    private val onItemDeleted: ((item: VideoProgress) -> Unit)? = null,
) : ListAdapter<VideoProgress, RecyclerView.ViewHolder>(DiffCallback()) {

    class ImageViewHolderItem(
        private val binding: ItemMediaFileBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: VideoProgress) {
            binding.apply {
                tvMenu.text = item.path
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ImageViewHolderItem {
        val binding =
            ItemMediaFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolderItem(binding)
            .apply {
                binding.tvMenu.setOnClickListener {
                    val progress = currentList[absoluteAdapterPosition]
                    onItemPlay?.invoke(progress)
                }
                binding.delete.setOnClickListener {
                    val progress = currentList[absoluteAdapterPosition]
                    onItemDeleted?.invoke(progress)
                }
                binding.sort.visibility = View.GONE
            }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ImageViewHolderItem).bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<VideoProgress>() {
        override fun areItemsTheSame(oldItem: VideoProgress, newItem: VideoProgress) =
            oldItem.path == newItem.path

        override fun areContentsTheSame(oldItem: VideoProgress, newItem: VideoProgress) =
            oldItem.path == newItem.path
                    && oldItem.lastTimestampe == newItem.lastTimestampe
                    && oldItem.currentPosition == newItem.currentPosition
    }
}