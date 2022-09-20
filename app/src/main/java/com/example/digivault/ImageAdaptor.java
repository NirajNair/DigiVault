package com.example.digivault;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ImageAdaptor extends BaseAdapter {
    LinkedHashMap<Integer, String> imgIds;
    private Context context;
    Encryption encryption = new Encryption();
    public ImageAdaptor(LinkedHashMap<Integer, String> imageIds, Context context) throws NoSuchAlgorithmException {
        this.imgIds = imageIds;
        this.context = context;
    }

    @Override
    public int getCount() {
        return imgIds.size();
    }

    @Override
    public Object getItem(int i) {
        return imgIds.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    // displays all the images
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView imgView = (ImageView) view;
        List<String> l = new ArrayList<String>(imgIds.values());
        try {
            if(imgView == null){
                imgView = new ImageView(context);
                // setting parameters to grid layout
                GridLayout.LayoutParams param= new GridLayout.LayoutParams(GridLayout.spec(
                        GridLayout.UNDEFINED,GridLayout.FILL,1f),
                        GridLayout.spec(GridLayout.UNDEFINED,GridLayout.FILL,1f));
                param.height = 350;
                param.width  = 350;
                imgView.setLayoutParams(param);
                imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
//            Log.i("l.get",l.get(i));

            byte[] imageBytes = encryption.decrypt(l.get(i));
//            Log.i("encrypted img", l.get(i));
            Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
//            Log.i("decrypted img", decryptedImage);
//            Log.i("before setting image", String.valueOf(i));
            imgView.setImageBitmap(decodedImage); //giving image ids to ImageView
//            Log.i("after setting image", String.valueOf(i));
        } catch (Exception e){
            e.printStackTrace();
        }
        return imgView;
    }

}
