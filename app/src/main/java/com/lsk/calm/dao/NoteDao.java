package com.lsk.calm.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lsk.calm.db.NoteDatabase;
import com.lsk.calm.bean.Note;

import java.util.ArrayList;
import java.util.List;

public class NoteDao {

    private SQLiteOpenHelper helper;
    private SQLiteDatabase database;

    // note表的各个属性
    private static final String[] cols = {
            NoteDatabase.ID,
            NoteDatabase.CONTENT,
            NoteDatabase.TIME,
            NoteDatabase.TAG
    };

    public NoteDao(Context context) {
        helper = new NoteDatabase(context);
    }

    // 打开数据库
    public void open() {
        database = helper.getWritableDatabase();
    }

    // 关闭数据库
    public void close() {
        helper.close();
    }

    // 添加一个note，并将这个note返回
    public Note addNote(Note note) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NoteDatabase.CONTENT, note.getContent());
        contentValues.put(NoteDatabase.TIME, note.getTime());
        contentValues.put(NoteDatabase.TAG, note.getTag());
        long id = database.insert(NoteDatabase.TABLE_NAME, null, contentValues);
        note.setId(id);
        return note;
    }


    // 获取数据库中的单个note
    public Note getOneNote(long id) {
        Cursor cursor = database.query(NoteDatabase.TABLE_NAME, cols, NoteDatabase.ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        Note n = new Note(cursor.getString(1), cursor.getString(2), cursor.getInt(3));
        return null;
    }

    // 获取数据库中tag为指定值的notes
    public List<Note> getTagNotes(int tag) {
        List<Note> notes = new ArrayList<>();
        Cursor cursor = database.query(NoteDatabase.TABLE_NAME, cols, NoteDatabase.TAG + "=?", new String[]{String.valueOf(tag)}, null, null, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Note note = new Note();
                note.setId(cursor.getLong(cursor.getColumnIndex(NoteDatabase.ID)));
                note.setContent(cursor.getString(cursor.getColumnIndex(NoteDatabase.CONTENT)));
                note.setTime(cursor.getString(cursor.getColumnIndex(NoteDatabase.TIME)));
                note.setTag(cursor.getInt(cursor.getColumnIndex(NoteDatabase.TAG)));
                notes.add(note);
            }
        }
        return notes;
    }

    // 获取数据库中的全部note
    public List<Note> getAllNotes() {
        Cursor cursor = database.query(NoteDatabase.TABLE_NAME, cols, null, null, null, null, null);
        List<Note> notes = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Note note = new Note();
                note.setId(cursor.getLong(cursor.getColumnIndex(NoteDatabase.ID)));
                note.setContent(cursor.getString(cursor.getColumnIndex(NoteDatabase.CONTENT)));
                note.setTime(cursor.getString(cursor.getColumnIndex(NoteDatabase.TIME)));
                note.setTag(cursor.getInt(cursor.getColumnIndex(NoteDatabase.TAG)));
                notes.add(note);
            }
        }
        return notes;
    }

    // 更新数据库中的某一个note
    public int updateNote(Note note) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NoteDatabase.CONTENT, note.getContent());
        contentValues.put(NoteDatabase.TIME, note.getTime());
        contentValues.put(NoteDatabase.TAG, note.getTag());
        return database.update(NoteDatabase.TABLE_NAME, contentValues,
                NoteDatabase.ID + "=?", new String[]{String.valueOf(note.getId())});
    }

    // 删除数据库中的一个note
    public void deleteNote(Note note) {
        database.delete(NoteDatabase.TABLE_NAME, NoteDatabase.ID + "=" + note.getId(), null);
    }

    // 查询最小的id值
    public long getMinId() {
        String sql = "select MIN(" + cols[0] + ") from " + ((NoteDatabase) helper).TABLE_NAME;
        Cursor cursor = database.rawQuery(sql, null);
        int a = -1;
        if (cursor.moveToFirst())
            a = cursor.getInt(0);
        return a;
    }

    // 查询最大的id值
    public long getMaxId() {
        String sql = "select MAX(" + cols[0] + ") from " + ((NoteDatabase) helper).TABLE_NAME;
        Cursor cursor = database.rawQuery(sql, null);
        int a = -1;
        if (cursor.moveToFirst())
            a = cursor.getInt(0);
        return a;
    }
}
