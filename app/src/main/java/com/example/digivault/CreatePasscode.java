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

public class CreatePasscode extends AppCompatActivity {

    EditText enterPasscode, confirmPasscode;
    Button submitPasscode;
    ImageButton backArrowBtn;
    private String uid, username;
    DBHelper dbHelper;
    public void printToast(String text){
        Toast.makeText(CreatePasscode.this, text, Toast.LENGTH_SHORT).show();
    }

    //runs whenever this activity starts
    @Override
    protected void onStart() {
        super.onStart();
        Intent i = getIntent();
        uid = i.getExtras().getString("uid");
        username = i.getExtras().getString("username");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_passcode);
        getSupportActionBar().hide();
        enterPasscode = (EditText) findViewById(R.id.enterPasscode);
        confirmPasscode = (EditText) findViewById(R.id.confirmPasscode);
        submitPasscode = (Button) findViewById(R.id.submitPasscode);
        backArrowBtn = (ImageButton) findViewById(R.id.backBtn);

        dbHelper = new DBHelper(this); // database helper obj, it consists of functions to help db operations

        // function helps in going back to signup page using back arrow button instead of mobiles's back button
        backArrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreatePasscode.this.finish();
                System.exit(0);
            }
        });

        submitPasscode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ePcode = enterPasscode.getText().toString();
                String cPcode = confirmPasscode.getText().toString();
                if(ePcode.isEmpty() || cPcode.isEmpty() || ePcode.length() < 4 || cPcode.length() < 4){
                    printToast("Please fill the fields properly.");
                } else {
                    if(ePcode.equals(cPcode)){
                        Log.i("uid", uid);
                        String hashedPasscode = md5Hashing(ePcode);
                        Boolean updateRes = dbHelper.updatePasscode(hashedPasscode, uid); // stores the result after inserting the user in db
                        if(updateRes){
                            Log.i("passcode updated", "true");
                            Intent intent = new Intent(getApplicationContext(), GalleryActivity.class); // moves to gallery page
                            intent.putExtra("username", username);
                            startActivity(intent);
                            finish();
                        } else{
                            printToast("Passcode could not be created.");
                        }
                    } else {
                        printToast("Passcodes do not match");
                    }
                }
            }
        });


    }
}