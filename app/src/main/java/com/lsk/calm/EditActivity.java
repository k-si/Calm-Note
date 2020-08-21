package com.lsk.calm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.lsk.calm.bean.Note;
import com.lsk.calm.dao.NoteDao;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.lsk.calm.MainActivity.Mode.CREATE_NOTE;
import static com.lsk.calm.MainActivity.Mode.DELETE_NOTE;
import static com.lsk.calm.MainActivity.Mode.OPEN_NOTE;
import static com.lsk.calm.MainActivity.Mode.UPDATE_NOTE;

public class EditActivity extends AppCompatActivity {

    private Context editContext = this;

    private EditText mEditText;
    private Spinner mSpinner;

    private long note_id;
    private int tag;
    private int mode;
    private String oldContent;

    private boolean isSync;
    private boolean isTagChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.in_righttoleft, R.anim.out_righttoleft); // 过渡动画，当在startActivity后面使用失效时在这里使用
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // 从sharedPreferences中获取tag信息
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(editContext);
        List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", null).split("_"));

        // 设置自定义的toolbar
        Toolbar toolbar = findViewById(R.id.edit_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Note");

        mEditText = findViewById(R.id.edit_text);
        mSpinner = findViewById(R.id.spinner);

        // 设置spinner的adapter
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(editContext, R.layout.spinner_item, tagList);
        mSpinner.setAdapter(spinnerAdapter);

        // 设置spinner的点击下拉事件，更换tag
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tag = (int) l + 1;
                isTagChange = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        // 获取点击note事件传来的mode
        Intent intentFromMain = getIntent();
        mode = intentFromMain.getIntExtra("mode", -1);
        tag = intentFromMain.getIntExtra("tag", 1);
        if (mode == OPEN_NOTE) { // mode=0表示打开已存在的note
            note_id = intentFromMain.getLongExtra("id", 0);
            oldContent = intentFromMain.getStringExtra("content");
            mEditText.setText(oldContent);
            mEditText.setSelection(oldContent.length()); // 设置光标在内容末尾
        }
        mSpinner.setSelection(tag - 1); // 当打开已存在的note时，spinner必须对应它的tag
    }

    // 设置右上角菜单栏的删除按钮、保存按钮
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit, menu);
        return true;
    }

    // 点击返回，将edittext的信息返回给mainactivity
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME)
            return true;
        else if (KeyEvent.KEYCODE_BACK == keyCode && event.getAction() == KeyEvent.ACTION_DOWN) {
            Intent intent = new Intent();
            String currentContent = mEditText.getText().toString(); // 注意需要加上toString，因为getText得到的值并不是string类型
            if (mode == CREATE_NOTE && currentContent.length() != 0 && !isSync) { // mode=1表示是新创建的note；isSync为true，表示已经通过同步按钮更新，不需要再传回数据
                intent.putExtra("content", currentContent);
                intent.putExtra("time", getTime());
                intent.putExtra("mode", CREATE_NOTE); // 告知mainactivit这是create操作
            }
            else if (mode == OPEN_NOTE && !isSync) { // mode=0表示是打开note进行更新的操作；isSync为true，表示已经通过同步按钮更新，不需要再传回数据
                if (!currentContent.equals(oldContent) || isTagChange) { // 当edittext中的内容和之前发生变化或tag改变
                    intent.putExtra("id", note_id); // 必须保证note和之前是同一个id，否则数据库会自动自增id
                    intent.putExtra("content", currentContent);
                    intent.putExtra("time", getTime());
                    intent.putExtra("mode", UPDATE_NOTE); // 告知mainactivit这是update操作
                }
            }
            intent.putExtra("tag", tag);
            setResult(RESULT_OK, intent);
            finish();
//            overridePendingTransition(R.anim.in_righttoleft, R.anim.out_righttoleft); // 过渡动画，当在startActivity后面使用失效时在这里使用
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 右上角菜单栏的在被选中时调用此方法
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final Intent intent = new Intent();
        switch (item.getItemId()) {
            case R.id.delete: // 删除note
                new AlertDialog.Builder(EditActivity.this) // 设置dialog对话框提示是否删除
                        .setTitle("提示")
                        .setIcon(R.drawable.ic_warning)
                        .setMessage("确定要删除这项备忘录？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (mode == CREATE_NOTE) {
                                    if (isSync) { // 当第一次创建note，而且通过sync按钮实现了同步，那么需要添加id，在mainactivity中执行数据库删除
                                        NoteDao dao = new NoteDao(editContext);
                                        dao.open();
                                        long id = dao.getMaxId();
                                        dao.close();
                                        intent.putExtra("mode", DELETE_NOTE);
                                        intent.putExtra("id", id);
                                        intent.putExtra("tag", tag);
                                    }
                                    setResult(RESULT_OK, intent); // 如果第一次创建note，并没有同步，那么直接返回主页面
                                } else {
                                    intent.putExtra("mode", DELETE_NOTE);
                                    intent.putExtra("id", note_id);
                                    intent.putExtra("tag", tag);
                                    setResult(RESULT_OK, intent);
                                }
                                finish();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss(); // 相当于关闭dialog，不做任何改动
                            }
                        })
                        .create()
                        .show();
                break;
            case R.id.sync: // 将note内容同步到本地，设置标识为为true，表示已经通过同步按钮保存
                String currentContent = mEditText.getText().toString();
                if (mode == OPEN_NOTE && currentContent.length() != 0) {
                    NoteDao dao = new NoteDao(EditActivity.this);
                    dao.open();
                    Note note = new Note(mEditText.getText().toString(), getTime(), tag);
                    note.setId(note_id);
                    dao.updateNote(note);
                    dao.close();
                    isSync = true;
                }
                else if (mode == CREATE_NOTE && (!currentContent.equals(oldContent) || isTagChange)) {
                    NoteDao dao = new NoteDao(EditActivity.this);
                    dao.open();
                    Note note = new Note(mEditText.getText().toString(), getTime(), tag);
                    dao.addNote(note);
                    dao.close();
                    isSync = true;
                }
                Toast.makeText(EditActivity.this, "已保存", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // 获取按下返回按钮的时间，也就是完成note的时间
    public String getTime() {
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }
}