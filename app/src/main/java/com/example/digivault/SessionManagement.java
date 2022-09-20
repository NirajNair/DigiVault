package com.example.digivault;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionManagement {
    SharedPreferences sharedPreferences; // this holds session info
    SharedPreferences.Editor editor; // editor to edit shared preferernces
    String SHARED_PREF_NAME = "session";
    String USERNAME = "username";
    String UID = "uid";

    public SessionManagement(Context context){
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,
                Context.MODE_PRIVATE); // creating a session obj
        editor = sharedPreferences.edit(); // creating an editor for the obj
    }
    // saves a session
    public void saveSession(User user){
        editor.putString(USERNAME, user.getUsername());
        editor.putString(UID, user.getUid());
        editor.commit();
    }

    public void deleteSession(){
        editor.putString(USERNAME, "");
        editor.putString(UID, "");
        editor.commit();
    }

    //return user whose session is saved
    public String getSession(){
        Log.i("user name from sess man", sharedPreferences.getString(UID, ""));
        return sharedPreferences.getString(UID, "");
    }

}
