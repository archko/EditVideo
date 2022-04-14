package com.thuypham.ptithcm.editvideo.ui.activity

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.thuypham.ptithcm.editvideo.databinding.ItemMenuBinding
import com.thuypham.ptithcm.editvideo.ui.adapter.BaseRecyclerAdapter
import com.thuypham.ptithcm.editvideo.ui.adapter.BaseViewHolder

class MergeAdapter(
    context: Context
) : BaseRecyclerAdapter<String>(context) {

    class MergeViewHolder(
        private val binding: ItemMenuBinding,
    ) : BaseViewHolder<String>(binding.root) {
        override fun onBind(data: String?, position: Int) {
            if (data != null) {
                binding.apply {
                    tvMenu.text = data
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): MergeViewHolder {
        val binding = ItemMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MergeViewHolder(binding).apply {
            /* binding.tvMenu.setOnSingleClickListener {
                 onItemSelected?.invoke(currentList[absoluteAdapterPosition])
             }*/
        }
    }
}