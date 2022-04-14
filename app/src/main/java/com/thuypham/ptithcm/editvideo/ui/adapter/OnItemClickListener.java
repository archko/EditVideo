package com.thuypham.ptithcm.editvideo.ui.adapter;

import android.view.View;

public interface OnItemClickListener<T> {
    void onItemClick(View view, int position, T data);
}
