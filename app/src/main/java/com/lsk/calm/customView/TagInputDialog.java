package com.lsk.calm.customView;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.lsk.calm.R;

public class TagInputDialog extends Dialog implements View.OnClickListener {

    private TextView mMessage, mCancel, mConfirm;
    private EditText mEdit;
    private String message, cancel, confirm;
    private ICancelListener oncancelListener;
    private IConfirmListener confirmListener;

    public TagInputDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    public TagInputDialog setCancel(String cancel, ICancelListener listener) {
        this.cancel = cancel;
        this.oncancelListener = listener;
        return this;
    }

    public TagInputDialog setConfirm(String confirm, IConfirmListener listener) {
        this.confirm = confirm;
        this.confirmListener = listener;
        return this;
    }

    public EditText getEditText() {
        return this.mEdit;
    }

    public TagInputDialog(@NonNull Context context) {
        super(context);
    }

    public TagInputDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_input_dialog);

        // 设置宽度
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        Point size = new Point();
        d.getSize(size);
        p.width = (int) (size.x * 0.9); // 设置dialog的宽度为当前屏幕的宽度的80%
        getWindow().setAttributes(p);

        mMessage = findViewById(R.id.message);
        mCancel = findViewById(R.id.cancel);
        mConfirm = findViewById(R.id.comfirm);
        mEdit = findViewById(R.id.tag);

        if (!TextUtils.isEmpty(message))
            mMessage.setText(message);
        if (!TextUtils.isEmpty(cancel))
            mCancel.setText(cancel);
        if (!TextUtils.isEmpty(confirm))
            mConfirm.setText(confirm);
        mCancel.setOnClickListener(this);
        mConfirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                if (oncancelListener != null)
                    oncancelListener.onCancel(this);
                this.dismiss();
                break;
            case R.id.comfirm:
                if (confirmListener != null)
                    confirmListener.onConfirm(this);
                this.dismiss();
                break;
        }
    }

    @Override
    public void dismiss() { // 重写dismiss方法解决软键盘不自动回收问题
        View view = getCurrentFocus();
        if (view instanceof TextView) {
            InputMethodManager mInputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        }
        super.dismiss();
    }

    public interface ICancelListener {
        void onCancel(TagInputDialog dialog);
    }

    public interface IConfirmListener {
        void onConfirm(TagInputDialog dialog);
    }
}
