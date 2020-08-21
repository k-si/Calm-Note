package com.lsk.calm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lsk.calm.adapter.NotesAdapter;
import com.lsk.calm.adapter.TagsAdapter;
import com.lsk.calm.bean.Note;
import com.lsk.calm.dao.NoteDao;
import com.lsk.calm.customView.TagInputDialog;
import com.lsk.calm.touchhelper.NoteTouchHelper;
import com.lsk.calm.touchhelper.OnItemTouchCallbackListener;
import com.lsk.calm.touchhelper.TouchCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnItemTouchCallbackListener {

    private RelativeLayout mainLayout;

    private SharedPreferences sharedPreferences;
    private Context mainContext = this;
    private ArrayList<Note> mNoteList;
    private ArrayList<Note> mAllNoteList;

    private Toolbar toolbar;
    private FloatingActionButton mAddNote;
    private RecyclerView mNoteRecyclerview;
    private RecyclerView.Adapter mNoteAdapter;

    private DisplayMetrics metrics;
    private WindowManager manager;
    private PopupWindow mPopupWindow;
    private PopupWindow mPopupWindowCover;
    private ViewGroup customView;
    private ViewGroup coverView;
    private TagsAdapter mTagAdapter;
    private ListView mTagListView;
    private TextView mAddTag;

    private int currentTag = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化sharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (!sharedPreferences.contains("tagListString")) {
            String defaultTag = "默认分组";
            editor.putString("tagListString", defaultTag);
            editor.commit();
        }

        // 初始化自定义的toolbar
        toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        // 初始化右下角按钮，跳转到输入页面
        mAddNote = findViewById(R.id.add_note);
        mAddNote.setOnClickListener(this);

        // 设置recyclerview的adapter，具体布局为网格视图，列数为2
        mNoteList = new ArrayList<>();
        mAllNoteList = new ArrayList<>();
        mNoteRecyclerview = findViewById(R.id.note_list);
        mNoteRecyclerview.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
        mNoteAdapter = new NotesAdapter(mNoteList, MainActivity.this);
        mNoteRecyclerview.setAdapter(mNoteAdapter);

        // 将note数据展示在recyclerview中，刚构建mainactivity时，需要展示默认分组的notes，tag为1
        refreshNoteList(1);

        // 处理长按拖动card
        NoteTouchHelper helper = new NoteTouchHelper(new TouchCallback(this));
        helper.setEnableDrag(true);
        helper.setEnableSwipe(true);
        helper.attachToRecyclerView(mNoteRecyclerview);

        // 初始化popupwindow
        LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        customView = (ViewGroup) inflater.inflate(R.layout.popup_window, null);
        coverView = (ViewGroup) inflater.inflate(R.layout.popup_window_cover, null);

        mainLayout = findViewById(R.id.main_layout);

        manager = getWindowManager();
        metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
    }

    // mainactivity实现onclicklistener接口，然后重写onclick方法，view直接将本类对象设置为listener即可
    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.add_note:
                intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("mode", Mode.CREATE_NOTE);
                intent.putExtra("tag", currentTag);
                startActivityForResult(intent, 0);
//                overridePendingTransition(R.anim.in_righttoleft, R.anim.out_righttoleft); // 平滑的切换activity
                break;

            case R.id.add_tag:
                int len = sharedPreferences.getString("tagListString", "").split("_").length;
                if (len < 10) { // 设置添加分组的响应事件
                    new TagInputDialog(mainContext)
                            .setMessage("新建分类")
                            .setConfirm("确定", new TagInputDialog.IConfirmListener() {
                                @Override
                                public void onConfirm(TagInputDialog dialog) {
                                    List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", null).split("_"));
                                    String newTagName = dialog.getEditText().getText().toString();
                                    System.out.println(newTagName);
                                    if (newTagName.isEmpty() || newTagName.trim().isEmpty())
                                        Toast.makeText(mainContext, "分组名称不能为空", Toast.LENGTH_SHORT).show();
                                    else if (!tagList.contains(newTagName)) {
                                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainContext);
                                        String oldTagListString = sharedPreferences.getString("tagListString", null);
                                        String newTagListString = oldTagListString + "_" + newTagName;
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("tagListString", newTagListString);
                                        editor.commit();
                                        refreshTagList(); // 添加分组结束后，刷新taglist
                                    } else
                                        Toast.makeText(mainContext, "分组名称重复", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setCancel("取消", new TagInputDialog.ICancelListener() {
                                @Override
                                public void onCancel(TagInputDialog dialog) {
                                }
                            })
                            .show();
                } else
                    Toast.makeText(mainContext, "分组数量达到上限", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    // 自定义toolbar的菜单栏设置，如果没有该设置，菜单栏将不显示
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        // 查询笔记
        MenuItem search = menu.findItem(R.id.find);
        SearchView view = (SearchView) search.getActionView();
        view.setMaxWidth(Integer.MAX_VALUE); // 设置searchview占满整个toolbar
        view.setBackgroundColor(Color.TRANSPARENT);
        view.setQueryHint("搜索备忘");
        view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { // 当有确认键的时候触发该函数，这里没有这个功能，不用修改
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) { // 当输入内容产生变化时调用
                ((NotesAdapter) mNoteAdapter).getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting:
                showPopupWindow(); // 展示popupwindow
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPopupWindow() {
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        mPopupWindowCover = new PopupWindow(coverView, width, height, false);
        mPopupWindowCover.setAnimationStyle(R.style.pwcAnimate);
        mPopupWindow = new PopupWindow(customView, (int) (width * 0.7), height, true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        mPopupWindow.setAnimationStyle(R.style.pwAnimate);

        // post旨在等待主页面加载完，需要等待menuitem加载完再加载popupwindow
        findViewById(R.id.main_layout).post(new Runnable() {
            @Override
            public void run() {
                mPopupWindowCover.showAtLocation(mainLayout, Gravity.NO_GRAVITY, 0, 0);
                mPopupWindow.showAtLocation(mainLayout, Gravity.NO_GRAVITY, 0, 0);

                mAddTag = customView.findViewById(R.id.add_tag);
                mTagListView = customView.findViewById(R.id.tag_list);

                coverView.setOnTouchListener(new View.OnTouchListener() { // 点击旁边的灰色空屏触发，使得popupwindow消失
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        mPopupWindow.dismiss();
                        return true;
                    }
                });
                mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() { // 当popupwindow消失的时候触发，使得coverview消失
                    @Override
                    public void onDismiss() {
                        mPopupWindowCover.dismiss();
                    }
                });

                // 给新增分组设置点击事件，具体逻辑查看重写的onclick方法
                mAddTag.setOnClickListener(MainActivity.this);

                // 给每个分组设置adapter，显示tagList
                refreshTagList();

                // 设置每个tag的长按删除事件
                mTagListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                        if (position == 0)
                            Toast.makeText(mainContext, "默认分组禁止删除", Toast.LENGTH_SHORT).show();
                        else if (position > 0) {
                            new AlertDialog.Builder(mainContext)
                                    .setTitle("删除该分组")
                                    .setIcon(R.drawable.ic_warning)
                                    .setMessage("该分组所有备忘将移动到默认分组")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int which) {
                                            int tag = position + 1;

                                            // 将该分组下的所有note的tag置为1，即归并到默认分组
                                            // 由于可能是在其他分组的时候对另一个分组进行删除，所以需要将当前的noteList转换为被删除的分组的noteList
                                            List<Note> mTmpList = new ArrayList<>();
                                            NoteDao dao = new NoteDao(MainActivity.this);
                                            dao.open();
                                            mTmpList.addAll(dao.getTagNotes(tag));
                                            for (int i = 0; i < mTmpList.size(); i++) {
                                                Note note = mTmpList.get(i);
                                                note.setTag(1);
                                                dao.updateNote(note);
                                            }

                                            // 由于tag和position是有对应关系的，所以当一个tag消失时，其他的tag需要减1
                                            for (int k = 0; k < mAllNoteList.size(); k++) {
                                                Note note = mAllNoteList.get(k);
                                                if (note.getTag() >= tag) {
                                                    note.setTag(note.getTag() - 1);
                                                    dao.updateNote(note);
                                                }
                                            }
                                            dao.close();

                                            // 将sharedPreferences中对应的tag删除
                                            List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", null).split("_"));
                                            List<String> newTagList = new ArrayList<>();
                                            newTagList.addAll(tagList);
                                            newTagList.remove(position);
                                            String newTagListString = TextUtils.join("_", newTagList);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString("tagListString", newTagListString);
                                            editor.commit();

                                            // 刷新taglist
                                            refreshTagList();

                                            // 刷新主页面到默认分组
                                            toolbar.setTitle("Calm备忘录");
                                            refreshNoteList(1);
                                        }
                                    })
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .create()
                                    .show();
                            return true;
                        }
                        return false;
                    }
                });

                // 给每个分组设置点击事件，点击某个分组就显示这个分组中的note
                mTagListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", null).split("_"));
                        currentTag = i + 1;
                        refreshNoteList(currentTag);
                        if (i == 0)
                            toolbar.setTitle("Calm备忘录");
                        else
                            toolbar.setTitle(tagList.get(i));
                        mPopupWindow.dismiss();
                        mPopupWindowCover.dismiss();
                    }
                });
            }
        });
    }

    // 用于处理从editactivity返回带来的结果，将edit中的数据写入数据库
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int returnMode = data.getIntExtra("mode", -1);
        long noteId = data.getLongExtra("id", 0);
        int tag = data.getIntExtra("tag", currentTag);

        // 在返回之后要保证toolbar的title和note所在分组一致
        List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", null).split("_"));
        if (tag == 1)
            toolbar.setTitle("Calm备忘录");
        else
            toolbar.setTitle(tagList.get(tag - 1));

        if (returnMode == Mode.CREATE_NOTE) { // create一个note
            String content = data.getExtras().getString("content");
            String time = data.getExtras().getString("time");
            Note note = new Note(content, time, tag);
            NoteDao dao = new NoteDao(MainActivity.this);
            dao.open();
            dao.addNote(note);
            dao.close();
        } else if (returnMode == Mode.UPDATE_NOTE) { // 更新note
            String content = data.getExtras().getString("content");
            String time = data.getExtras().getString("time");
            Note note = new Note(content, time, tag);
            note.setId(noteId);
            NoteDao dao = new NoteDao(MainActivity.this);
            dao.open();
            dao.updateNote(note);
            dao.close();
        } else if (returnMode == Mode.DELETE_NOTE) { // 删除note
            Note note = new Note();
            note.setId(noteId);
            NoteDao dao = new NoteDao(MainActivity.this);
            dao.open();
            dao.deleteNote(note);
            dao.close();
        }
        refreshNoteList(tag); // 返回结果后要重新刷新包含note的recyclerview
    }

    // 从数据库获取note数据，并将其显示在recyclerview中
    private void refreshNoteList(int tag) {
        NoteDao dao = new NoteDao(MainActivity.this);
        dao.open();
        mNoteList.clear(); // 清空存放note的arraylist，因为刷新的时候需要重新从数据库获取数据
        mAllNoteList.clear();
        mNoteList.addAll(dao.getTagNotes(tag));
        mAllNoteList.addAll(dao.getAllNotes());
        dao.close();
        mNoteAdapter.notifyDataSetChanged(); // 使adapter重新响应
    }

    // 刷新tag列表
    private void refreshTagList() {

        // 从sharedPreferences获取tagname，然后从遍历notelist，统计出各个tag对应的note数量
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainContext);
        List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", null).split("_"));
        Integer[] noteNumList = new Integer[tagList.size()];
        for (int i = 0; i < noteNumList.length; i++)
            noteNumList[i] = 0;
        for (int i = 0; i < mAllNoteList.size(); i++)
            noteNumList[mAllNoteList.get(i).getTag() - 1]++;

        // 设置adapter
        mTagAdapter = new TagsAdapter(mainContext, tagList, Arrays.asList(noteNumList));
        mTagListView.setAdapter(mTagAdapter);
        mTagAdapter.notifyDataSetChanged();
    }

    // 处理滑动删除
    @Override
    public void onSwiped(int adapterPosition) {
        // 由于当前为2列的网格视图，暂时不添加滑动删除功能
    }

    // 处理拖拽换位
    @Override
    public boolean onMove(int from, int to) {
        if (from < to)
            for (int i = from; i < to; i++)
                Collections.swap(mNoteList, i, i + 1);
        else
            for (int i = from; i > to; i--)
                Collections.swap(mNoteList, i, i - 1);
        mNoteAdapter.notifyItemMoved(from, to); // adapter重新进行布局
        return true;
    }

    // 当手指松开的时候（拖拽完成的时候）调用
    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        NoteDao dao = new NoteDao(MainActivity.this);
        dao.open();
        List<Long> idList = new ArrayList<>();
        for (int i = 0; i < mNoteList.size(); i++)
            idList.add(mNoteList.get(i).getId());
        Collections.sort(idList); // 由于在手指松开后，noteList中的顺序已经方生了改变，所以要重新进行排序
        for (int i = 0; i < mNoteList.size(); i++) { // 遍历所有的note，更新他们的id，并在数据库中进行更新
            Note note = mNoteList.get(i);
            note.setId(idList.get(i)); // 位置变化之后，原位置的id不变，对note的内容进行更新
            dao.updateNote(note);
        }
        dao.close();
    }

    public static class Mode {
        public static final int OPEN_NOTE = 0;
        public static final int CREATE_NOTE = 1;
        public static final int UPDATE_NOTE = 2;
        public static final int DELETE_NOTE = 3;
    }
}