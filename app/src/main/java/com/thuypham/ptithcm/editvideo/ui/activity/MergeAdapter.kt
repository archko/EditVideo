package com.thuypham.ptithcm.editvideo.ui.activity

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.thuypham.ptithcm.editvideo.databinding.ItemMediaFileBinding
import com.thuypham.ptithcm.editvideo.extension.setOnSingleClickListener
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.ui.adapter.BaseRecyclerAdapter
import com.thuypham.ptithcm.editvideo.ui.adapter.BaseViewHolder

class MergeAdapter(
    context: Context,
    private val onItemDeleted: ((item: MediaFile, pos: Int) -> Unit)? = null,
) : BaseRecyclerAdapter<MediaFile>(context) {

    class MergeViewHolder(
        private val binding: ItemMediaFileBinding,
    ) : BaseViewHolder<MediaFile>(binding.root) {
        override fun onBind(data: MediaFile?, position: Int) {
            if (data != null) {
                binding.apply {
                    tvMenu.text = data.path
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): MergeViewHolder {
        val binding =
            ItemMediaFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MergeViewHolder(binding).apply {
            binding.delete.setOnSingleClickListener {
                onItemDeleted?.invoke(
                    currentList[absoluteAdapterPosition],
                    absoluteAdapterPosition
                )
            }
        }
    }
}