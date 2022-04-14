package com.thuypham.ptithcm.editvideo.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class BaseRecyclerAdapter<T> extends ListAdapter<T, BaseViewHolder> {

    protected final LayoutInflater mInflater;
    protected List<T> mData;

    public BaseRecyclerAdapter(Context context) {
        this(context, null, null);
    }

    public BaseRecyclerAdapter(Context context, List<T> data, DiffUtil.ItemCallback diffCallback) {
        super(diffCallback);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mData = data;
        if (null == mData) {
            mData = new ArrayList<>();
        }
    }

    public LayoutInflater getInflater() {
        return mInflater;
    }

    public void setData(@Nullable List<T> data) {
        this.mData = data;
        if (mData == null) {
            mData = new ArrayList<>();
        }
    }

    public void addData(@Nullable List<T> arrayList) {
        if (mData == null) {
            mData = new ArrayList();
        }
        if (null != arrayList) {
            mData.addAll(arrayList);
        }
    }

    public List<T> getData() {
        return mData;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder viewHolder, final int position) {
        T itemData = getItemData(position);
        viewHolder.attach(position, itemData);
        viewHolder.onBind(itemData, position);
    }

    public T getItemData(int position) {
        return mData.get(position);
    }

    @Override
    public int getItemCount() {
        int size = mData == null ? 0 : mData.size();
        return size;
    }

    public void add(T item) {
        int position = getData().size();
        getData().add(item);
        notifyItemInserted(position);
    }

    public void add(int position, T item) {
        getData().add(position, item);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        getData().remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getData().size());
    }
}