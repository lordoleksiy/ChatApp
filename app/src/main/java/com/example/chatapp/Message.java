package com.example.chatapp;

import java.util.Date;

public class Message {
    private String text;
    private String author;
    private long time;

    public Message(String text, String author) {
        this.text = text;
        this.author = author;
        time = new Date().getTime();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
