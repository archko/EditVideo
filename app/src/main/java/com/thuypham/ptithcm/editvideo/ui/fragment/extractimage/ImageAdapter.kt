package com.thuypham.ptithcm.editvideo.ui.fragment.extractimage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.databinding.ItemMediaBinding
import com.thuypham.ptithcm.editvideo.extension.setOnSingleClickListener
import com.thuypham.ptithcm.editvideo.model.MediaFile
import java.io.File

class ImageAdapter(
    private val onItemSelected: ((item: MediaFile) -> Unit)? = null,
) : ListAdapter<MediaFile, RecyclerView.ViewHolder>(DiffCallback()) {

    class ImageViewHolderItem(
        private val binding: ItemMediaBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MediaFile) {
            binding.apply {
                if (!item.path.isNullOrEmpty()) {
                    if (item.path!!.startsWith("/storage")) {
                        ivMedia.load(item.path?.let { File(it) }) {
                            crossfade(true)
                            diskCachePolicy(CachePolicy.DISABLED)
                            placeholder(R.drawable.ic_image_placeholder)
                        }
                    } else {
                        ivMedia.load(item.path?.let { File(it) }) {
                            crossfade(true)
                            placeholder(R.drawable.ic_image_placeholder)
                        }
                    }
                }
                tvName.text = item.displayName
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ImageViewHolderItem {
        val binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolderItem(binding)
            .apply {
                binding.root.setOnSingleClickListener {
                    val filePath = currentList[absoluteAdapterPosition]
                    onItemSelected?.invoke(filePath)
                }
            }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ImageViewHolderItem).bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<MediaFile>() {
        override fun areItemsTheSame(oldItem: MediaFile, newItem: MediaFile) =
            oldItem.path == newItem.path

        override fun areContentsTheSame(oldItem: MediaFile, newItem: MediaFile) =
            oldItem.path == newItem.path
    }
}