package com.example.digivault;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// IN progresss
public class ImageDBHelper extends SQLiteOpenHelper {
    public ImageDBHelper(Context context)  {
        super(context, "Image.db", null, 1);
    }
    SQLiteDatabase imageDBHelper = this.getWritableDatabase();

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table imagedb (username Text, imageID Text, imageName Text, image Text, imageLocation Text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop table if exists imagedb");
    }

    public boolean insertImage(String username, String imageId, String imagename, String img, String imageLocation){
        ContentValues map = new ContentValues();
        map.put("username", username);
        map.put("imageID", imageId);
        map.put("imageName", imagename);
        map.put("image", img);
        map.put("imageLocation", imageLocation);
        long result = imageDBHelper.insert("imagedb", null, map);
        if(result == -1) return false;
        else return true;
    }
    // updates the cropped image
    public Boolean updateImage(String img, String imgId){
        ContentValues map = new ContentValues(); // kind of dict which is used to insert values in db
        map.put("image", img);
        long result = imageDBHelper.update("imagedb", map, "imageID=?", new String[]{imgId}); // holds int result after insertion
        if(result == -1) return false;
        else return true;
    }
    public LinkedHashMap<Integer, String> getListOfImages(String username){
        LinkedHashMap<Integer, String> imageList = new LinkedHashMap<Integer, String>();
        List<Integer> imageIdList = new ArrayList<>();
        SQLiteDatabase imageDBHelper = this.getWritableDatabase();
        try{
            if(imageIdList != null){
                imageIdList = getCountOfImages(username);
                for(int img:imageIdList){
                    Log.i("img id ", String.valueOf(img));
                    Cursor cursor = imageDBHelper.rawQuery("select image from imagedb where imageID=?",new String[] {String.valueOf(img)});
                    Log.i("curosr", String.valueOf(cursor.getCount()));
                    cursor.moveToFirst();
                    String text = cursor.getString(0);
                    cursor.close();
                    imageList.put(img, text);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
//        Log.i("image list ", imageList.get(0));
        return imageList;
    }

    public int deleteImage(List<Integer> imageList){
        int rows =0;
        for(Integer img:imageList){
            rows += imageDBHelper.delete("imagedb", "imageID = ?", new String[]{String.valueOf(img)});
        }
        Log.i("rows ", String.valueOf(rows));
        return rows;
//        imageDBHelper.execSQL("delete * from imagedb where img = ? and username = ?", new String[] {image, username});
    }

    public int getLastImageId(String username){
        Cursor cursor = imageDBHelper.rawQuery("select imageID from imagedb where username=?",
                new String[] {username});
        int lastId = cursor.getCount();
        cursor.close();
        return lastId;
    }

    public String getImageName(String imageId){
        Log.i("image getImageName", imageId);
        Cursor cursor = imageDBHelper.rawQuery("select imageName from imagedb where imageID=?",
                new String[] {imageId});
        cursor.moveToFirst();
        String text = cursor.getString(0);
        cursor.close();
//        Log.i("cursor", String.valueOf(cursor.getString(cursor.getColumnIndex("imageName"))));
        return text;
    }
    public String getImage(String imageId){
        Log.i("image getImageName", imageId);
        Cursor cursor = imageDBHelper.rawQuery("select image from imagedb where imageID=?",
                new String[] {imageId});
        cursor.moveToFirst();
        String text = cursor.getString(0);
        cursor.close();
//        Log.i("cursor", String.valueOf(cursor.getString(cursor.getColumnIndex("imageName"))));
        return text;
    }
    public String getImgLocation(String imageId){
        Log.i("image getImageName", imageId);
        Cursor cursor = imageDBHelper.rawQuery("select imageLocation from imagedb where imageID=?",
                new String[] {imageId});
        cursor.moveToFirst();
        String text = cursor.getString(0);
        cursor.close();
//        Log.i("cursor", String.valueOf(cursor.getString(cursor.getColumnIndex("imageName"))));
        return text;
    }

    public List<Integer> getCountOfImages(String username){
        List<Integer> imageIdList = new ArrayList<>();
        try{
            Cursor cursor = imageDBHelper.rawQuery("select imageID from imagedb where username=?",new String[] {username});
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                imageIdList.add(cursor.getInt(0));
                cursor.moveToNext();
            }
            cursor.close();
        } catch (Exception e){
            e.printStackTrace();
        }
//        Log.i("image list ", imageList.get(0));
        return imageIdList;
    }


}
