package com.thuypham.ptithcm.editvideo.ui.adapter;

import android.view.View;

import androidx.annotation.IdRes;
import androidx.recyclerview.widget.RecyclerView;

public class BaseViewHolder<T> extends RecyclerView.ViewHolder {

    protected int position;
    protected T data;

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public final void attach(int position, T data) {
        this.position = position;
        this.data = data;
    }

    public void onBind(final T data, int position) {

    }

    protected <T extends View> T findViewById(@IdRes int viewId) {
        return itemView.findViewById(viewId);
    }

    public T getData() {
        return data;
    }

    public void onViewRecycled() {

    }

    public void onViewAttachedToWindow() {

    }

    public void onViewDetachedFromWindow() {

    }

}
