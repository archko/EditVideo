package com.thuypham.ptithcm.editvideo.ui.adapter;

import android.view.View;

import androidx.annotation.IdRes;
import androidx.recyclerview.widget.RecyclerView;

public class BaseViewHolder<T> extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    private OnItemClickListener<T> onItemClickListener;

    private OnItemLongClickListener<T> onItemLongClickListener;

    protected int position;
    protected T data;

    public BaseViewHolder(View itemView) {
        this(itemView, null, null);
    }

    public BaseViewHolder(View itemView, OnItemClickListener<T> onItemClickListener) {
        this(itemView, onItemClickListener, null);
    }

    public BaseViewHolder(View itemView, OnItemClickListener<T> onItemClickListener, OnItemLongClickListener<T> onItemLongClickListener) {
        super(itemView);
        this.onItemClickListener = onItemClickListener;
        this.onItemLongClickListener = onItemLongClickListener;
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

    @Override
    public final void onClick(View v) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(v, position, data);
        }
    }

    @Override
    public final boolean onLongClick(View v) {
        if (onItemLongClickListener != null) {
            return onItemLongClickListener.onItemLongClick(v, position, data);
        }
        return false;
    }

    public final void setOnItemClickListener(OnItemClickListener<T> listener) {
        this.onItemClickListener = listener;
    }

    public final OnItemClickListener<T> getOnItemClickListener() {
        return onItemClickListener;
    }

    public final void setOnItemLongClickListener(OnItemLongClickListener<T> listener) {
        this.onItemLongClickListener = listener;
    }

    public final OnItemLongClickListener<T> getOnItemLongClickListener() {
        return onItemLongClickListener;
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
