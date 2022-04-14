package com.thuypham.ptithcm.editvideo.util;

import android.graphics.Color;

import com.thuypham.ptithcm.editvideo.ui.adapter.BaseRecyclerAdapter;

import java.util.Collections;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ItemTouchCallback extends androidx.recyclerview.widget.ItemTouchHelper.Callback {

    private BaseRecyclerAdapter adapter;

    public ItemTouchCallback(BaseRecyclerAdapter adapter) {
        this.adapter = adapter;
    }

    //线性布局和网格布局都可以使用
    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlag = 0;
        int swipeFlag = 0;
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        } else if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            swipeFlag = ItemTouchHelper.END;
        }
        return makeMovementFlags(dragFlag, swipeFlag);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        //得到当拖拽的viewHolder的Position
        int fromPosition = viewHolder.getAdapterPosition();
        //拿到当前拖拽到的item的viewHolder
        int toPosition = target.getAdapterPosition();
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(adapter.getData(), i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(adapter.getData(), i, i - 1);
            }
        }
        adapter.notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        //侧滑删除可以使用；
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    /**
     * 长按选中Item的时候开始调用
     * 长按高亮
     *
     * @param viewHolder
     * @param actionState
     */
    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            viewHolder.itemView.setBackgroundColor(Color.RED);
            //获取系统震动服务//震动70毫秒
            //Vibrator vib = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            //vib.vibrate(70);
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    /**
     * 手指松开的时候还原高亮
     *
     * @param recyclerView
     * @param viewHolder
     */
    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        viewHolder.itemView.setBackgroundColor(0);
        adapter.notifyDataSetChanged();  //完成拖动后刷新适配器，这样拖动后删除就不会错乱
    }
}