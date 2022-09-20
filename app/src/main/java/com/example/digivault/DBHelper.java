package com.example.digivault;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context) {
        super(context, "User.db", null, 201);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table user(uid Text primary key,username Text, password Text, passcode Text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop table if exists user");
    }

    SQLiteDatabase userDb = this.getWritableDatabase(); // creates a writable obj of db

    // inserts a user into the db
    public Boolean insertUser(String uid,String username, String password){
        ContentValues map = new ContentValues(); // kind of dict which is used to insert values in db
        map.put("uid", uid);
        map.put("username", username);
        map.put("password", password);
        map.put("passcode", "null");
        long result = userDb.insert("user", null, map); // holds int result after insertion
        if(result == -1) return false;
        else return true;
    }

    // updates the new passcode
    public Boolean updatePasscode(String passcode, String uid){
        ContentValues map = new ContentValues(); // kind of dict which is used to insert values in db
        map.put("passcode", passcode);
        long result = userDb.update("user", map, "uid=?", new String[]{uid}); // holds int result after insertion
        if(result == -1) return false;
        else return true;
    }

    // checks whether passcode is mapped to user or not
    public Boolean checkPasscodeIsNull(String uid){
        Cursor cursor = userDb.rawQuery("select passcode from user where uid = ?",
                new String[] {uid}); // cursor holds the result tuple in it after the query is run
        cursor.moveToFirst();
        String text = cursor.getString(0);
        cursor.close();
        if(text.equals("null"))
            return true;
        return false;
    }
    // checks if passcode entered is correct
    public Boolean checkPasscode(String passcode, String uid){
        Cursor cursor = userDb.rawQuery("select username from user where passcode = ? and uid = ?",
                new String[] {passcode, uid}); // cursor holds the result tuple in it after the query is run
        long count = cursor.getCount();
        cursor.close();
        if(count > 0) return true;
        else return false;
    }

    // checks if user exists in db
    public Boolean checkUsername(String username){
        Cursor cursor = userDb.rawQuery("select * from user where username = ?",
                new String[] {username}); // cursor holds the result tuple in it after the query is run
        long count = cursor.getCount();
        cursor.close();
        if(count > 0) return true;
        else return false;
    }

    // function to check if username and password are correct for a user
    public Boolean checkUsernameAndPassword(String username, String password){
        Cursor cursor = userDb.rawQuery("select * from user where username=? and password=?",
                new String[] {username, password});
        long count = cursor.getCount();
        cursor.close();
        if(count > 0) return true;
        else return false;
    }

    public String getUsername(String uid){
        Cursor cursor = userDb.rawQuery("select username from user where uid=?",
                new String[] {uid});
        cursor.moveToFirst();
        String text = cursor.getString(0);
        Log.i("username", cursor.getString(0)); // logging helps in debugging check docs for more info
        cursor.close();
        return text;
    }

    public String getUUid(String username){
        Cursor cursor = userDb.rawQuery("select uid from user where username=?",
                new String[] {username});
        cursor.moveToFirst();
        Log.i("uid", cursor.getString(0)); // logging helps in debugging check docs for more info
        String text = cursor.getString(0);
        cursor.close();
        return text;
    }
}
