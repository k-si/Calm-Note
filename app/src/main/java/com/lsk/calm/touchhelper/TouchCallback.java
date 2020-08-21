package com.lsk.calm.touchhelper;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class TouchCallback extends ItemTouchHelper.Callback {

    private boolean isEnableSwipe; // 允许滑动
    private boolean isEnableDrag; // 允许拖动
    private OnItemTouchCallbackListener callbackListener; // 回调接口

    public TouchCallback(OnItemTouchCallbackListener callbackListener) {
        this.callbackListener = callbackListener;
    }

    // 得到滑动或拖拽的方向
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {// GridLayoutManager
            // 可拖动的方向为上下左右
            int dragFlag = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            // 可滑动的方向为0，即不能左右快速滑动
            int swipeFlag = 0;
            return makeMovementFlags(dragFlag, swipeFlag);
        }
        return 0;
    }

    // 拖拽item时产生回调
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        if (this.callbackListener != null)
            this.callbackListener.onMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return false;
    }

    // 滑动删除时的回调
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if (this.callbackListener != null) {
            this.callbackListener.onSwiped(viewHolder.getAdapterPosition());
        }
    }

    // 当手指松开的时候（拖拽完成的时候）调用
    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        if (this.callbackListener != null)
            this.callbackListener.clearView(recyclerView, viewHolder);
    }

    // 是否可以长按拖拽
    @Override
    public boolean isLongPressDragEnabled() {
        return isEnableDrag;
    }

    // 是否可以滑动删除
    @Override
    public boolean isItemViewSwipeEnabled() {
        return isEnableSwipe;
    }

    // 对外部开放，设置enableDrag为true时可以拖动
    public void setEnableDrag(boolean enableDrag) {
        this.isEnableDrag = enableDrag;
    }

    // 设置enableSwipe为true时可以左右滑动
    public void setEnableSwipe(boolean enableSwipe) {
        this.isEnableSwipe = enableSwipe;
    }
}
