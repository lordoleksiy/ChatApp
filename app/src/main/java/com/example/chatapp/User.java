package com.example.chatapp;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class User {
    public ArrayList array = new ArrayList();
    public String name;
    public User(String name){
        this.name = name;
    }


    void add(String elem){
        array.add(elem);
    }

    ArrayList get(){
        return array;
    }
    @NonNull
    @Override
    public String toString(){
        return name;
    }
}
