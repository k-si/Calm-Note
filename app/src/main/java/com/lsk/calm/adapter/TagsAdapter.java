package com.lsk.calm.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import com.lsk.calm.R;

import java.util.List;

public class TagsAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> mTagList;
    private List<Integer> mNumList;

    public TagsAdapter(Context context, List<String> tagList, List<Integer> numList) {
        this.mContext = context;
        this.mTagList = tagList;
        this.mNumList = numList;
    }

    @Override
    public int getCount() {
        return mTagList.size();
    }

    @Override
    public Object getItem(int i) {
        return mTagList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = View.inflate(mContext, R.layout.tag_item, null);
        TextView tagName = v.findViewById(R.id.tag_name);
        TextView noteNum = v.findViewById(R.id.note_num);
        tagName.setText(mTagList.get(i));
        noteNum.setText(mNumList.get(i).toString()); // 注意传入的值为integer，这里必须转换
        return v;
    }
}
