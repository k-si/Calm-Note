package com.lsk.calm.touchhelper;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;

public class NoteTouchHelper extends ItemTouchHelper {

    private TouchCallback mCallback;

    public NoteTouchHelper(@NonNull TouchCallback callback) {
        super(callback);
        this.mCallback = callback;
    }

    public void setEnableDrag(boolean enableDrag) {
        mCallback.setEnableDrag(enableDrag);
    }

    public void setEnableSwipe(boolean enableSwipe) {
        mCallback.setEnableSwipe(enableSwipe);
    }
}
