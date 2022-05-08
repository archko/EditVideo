package com.thuypham.ptithcm.editvideo.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

abstract class AbsListAdapter<T>(diffCallback: DiffUtil.ItemCallback<T>) :
    ListAdapter<T, BaseViewHolder<T>>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
        return BaseViewHolder(null)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
    }
}