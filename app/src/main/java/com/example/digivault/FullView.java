package com.example.digivault;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

public class FullView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_view);
        ImageView imageView = findViewById(R.id.imgFull);
        int imgId = getIntent().getExtras().getInt("imageId");
        imageView.setImageResource(imgId);
    }
}