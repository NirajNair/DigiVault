package com.example.digivault;

import static com.example.digivault.MainActivity.md5Hashing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class Passcode extends AppCompatActivity {

    EditText passcode;
    Button submitPasscode;
    ImageButton backArrowBtn;
    private String uid;
    DBHelper dbHelper;

    public void printToast(String text){
        Toast.makeText(Passcode.this, text, Toast.LENGTH_SHORT).show();
    }

    //runs whenever this activity starts
    @Override
    protected void onStart() {
        super.onStart();
        Intent i = getIntent();
        uid = i.getExtras().getString("uid"); // this is how you get the message sent with the intent
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passcode);
        getSupportActionBar().hide();

        passcode = (EditText) findViewById(R.id.passcode);
        submitPasscode = (Button) findViewById(R.id.submitPasscode);
        backArrowBtn = (ImageButton) findViewById(R.id.backBtn);

        dbHelper = new DBHelper(this); // database helper obj, it consists of functions to help db operations

        // function helps in going back to signup page using back arrow button instead of mobiles's back button
        backArrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Passcode.this.finish();
                System.exit(0);
            }
        });

        submitPasscode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Pcode = passcode.getText().toString();
                if(Pcode.isEmpty() || Pcode.length() < 4){
                    printToast("Please fill the fields properly.");
                } else {
                    String hashedPcode = md5Hashing(Pcode);
                    Boolean res = dbHelper.checkPasscode(hashedPcode, uid); // holds the result after checking the creds
                    if(res){
                        Intent intent = new Intent(getApplicationContext(), GalleryActivity.class);
                        String username = dbHelper.getUsername(uid);
                        intent.putExtra("username", username);
                        startActivity(intent); // goes to gallery page
                        finish();
                    } else{
                        printToast("Invalid Passcode");
                    }
                }
            }
        });

    }
}