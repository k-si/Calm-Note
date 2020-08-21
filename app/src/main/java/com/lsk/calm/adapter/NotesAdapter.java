package com.lsk.calm.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lsk.calm.EditActivity;
import com.lsk.calm.MainActivity;
import com.lsk.calm.bean.Note;

import java.util.ArrayList;
import java.util.List;

import com.lsk.calm.R;

import static com.lsk.calm.MainActivity.Mode.OPEN_NOTE;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> implements Filterable {

    private List<Note> mNotesData;
    private List<Note> mFilterData;
    private Context mContext;

    public NotesAdapter(List<Note> notes, Context context) {
        this.mNotesData = notes;
        this.mFilterData = notes;
        this.mContext = context;
    }

    // 创建一个viewholder
    @NonNull
    @Override
    public NotesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 获取子项的view对象，将note_item和viewholder联系起来
        View view = LayoutInflater.from(mContext).inflate(R.layout.note_item, parent, false);
        return new ViewHolder(view);
    }

    // 从arraylist中找到当前view，并将数据绑定到view中
    @Override
    public void onBindViewHolder(@NonNull NotesAdapter.ViewHolder holder, int position) {
        Note currentNote = mFilterData.get(position); // 注意是过滤的note，在构造函数中mFilterData = notes
        holder.bindTo(currentNote);
    }

    // 获取recyclerview的包含子项的个数
    @Override
    public int getItemCount() {
        return mFilterData.size(); // 注意是过滤的note，在构造函数中mFilterData = notes
    }

    // 提供自定义的filter
    @Override
    public Filter getFilter() {
        return new MyFilter();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTitle;
        private TextView mSubtitle;
        private TextView mTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mTitle = itemView.findViewById(R.id.title);
            mSubtitle = itemView.findViewById(R.id.subtitle);
            mTime = itemView.findViewById(R.id.time);

            // 这里的itemview指代整个的note，其中包括了note的title、subtitle、time
            // 这样整个的单位设置监听器可以保证更好的监听事件成功执行
            itemView.setOnClickListener(this);
        }

        // 将数据绑定到textview上
        public void bindTo(Note currentNote) {
            String[] lines = currentNote.getContent().split("\n"); // 将文本信息的每一行分开

            // 当行数只有一行时，title和subtitle都设置为第一行
            // 当行数大于一行时，title设置为第一行，subtitle设置为除了第一行的所有内容，但是subtitle最多只会显示两行
            if (lines.length == 1) {
                mTitle.setText(lines[0]);
                mSubtitle.setText(lines[0]);
            } else if (lines.length > 1) {
                mTitle.setText(lines[0]);
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < lines.length; i++)
                    sb.append(lines[i]);
                mSubtitle.setText(sb.toString());
            }
            mTime.setText(currentNote.getTime());
        }

        // 击note列表中的一个note，跳转到editactivity
        @Override
        public void onClick(View view) {
            System.out.println("点击了");
            Note note = mFilterData.get(getAdapterPosition()); // 注意是过滤的note，在构造函数中mFilterData = notes
            Intent intent = new Intent(mContext, EditActivity.class);
            intent.putExtra("content", note.getContent());
            intent.putExtra("id", note.getId());
            intent.putExtra("time", note.getTime());
            intent.putExtra("tag", note.getTag());
            intent.putExtra("mode", OPEN_NOTE); // 传入mode=0表示从主页面打开一个note
            ((MainActivity) mContext).startActivityForResult(intent, 1);
        }
    }


    public class MyFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) { // 执行过滤操作
            if (charSequence.toString().isEmpty())
                mFilterData = mNotesData; // 没有要过滤的内容
            else {
                List<Note> mFilteredList = new ArrayList<>(); // 保存已经过滤完的数据
                for (Note note : mNotesData)
                    if (note.getContent().contains(charSequence.toString())) // 定义过滤规则
                        mFilteredList.add(note);
                mFilterData = mFilteredList;
            }
            FilterResults results = new FilterResults();
            results.values = mFilterData;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) { // 将过滤完的操作返回
            mFilterData = (List<Note>) filterResults.values;
            notifyDataSetChanged();
        }
    }
}
