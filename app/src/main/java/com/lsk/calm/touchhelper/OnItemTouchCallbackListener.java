package com.lsk.calm.touchhelper;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public interface OnItemTouchCallbackListener {

    // 当item被滑动时回调
    void onSwiped(int adapterPosition);

    // 当两个item位置互换时被回调，处理了操作返回true，没有处理返回false
    boolean onMove(int from, int to);

     // 当手指松开的时候（拖拽完成的时候）调用
    void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder);
}
