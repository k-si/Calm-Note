package com.lsk.calm.bean;

public class Note {

    private long id;
    private String time;
    private String content;
    private int tag;

    public Note() {

    }

    public Note(String content, String time, int tag) {
        this.time = time;
        this.content = content;
        this.tag = tag;
    }

    public long getId() {
        return id;
    }

    public String getTime() {
        return time;
    }

    public String getContent() {
        return content;
    }

    public int getTag() {
        return tag;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return content + "\n" + time.substring(5,16) + " "+ id;
    }
}
