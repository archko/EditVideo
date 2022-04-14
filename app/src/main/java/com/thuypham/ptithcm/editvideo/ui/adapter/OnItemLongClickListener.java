package com.thuypham.ptithcm.editvideo.ui.adapter;

import android.view.View;

public interface OnItemLongClickListener<T> {
    boolean onItemLongClick(View view, int position, T data);
}