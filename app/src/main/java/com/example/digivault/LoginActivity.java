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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    EditText username, password;
    Button signInBtn;
    ImageButton backArrowBtn;
    DBHelper dbHelper;
    String userSignedIn;

    public void printToast(String text){
        Toast.makeText(LoginActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    private void setSession(String uid, String username){
        User user = new User(uid, username);
        SessionManagement sessionManagement = new SessionManagement(LoginActivity.this);
        sessionManagement.saveSession(user);
    }

    @Override
    protected void onStart(){
        try {
            super.onStart();
            SessionManagement sessionManagement = new SessionManagement(LoginActivity.this);
            userSignedIn = sessionManagement.getSession();
            if(userSignedIn != ""){
                Intent intent = new Intent(getApplicationContext(), Passcode.class);
                intent.putExtra("username", userSignedIn);
                startActivity(intent);

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    // function runs when you press back button on mobile
    @Override
    public void onBackPressed(){
        SessionManagement sessionManagement = new SessionManagement(LoginActivity.this);
        userSignedIn = sessionManagement.getSession();
        if(userSignedIn == ""){
            printToast("Please Login!");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide(); // hides default action bar on top

        username = (EditText) findViewById(R.id.loginUsername);
        password = (EditText) findViewById(R.id.loginPassword);

        signInBtn = (Button) findViewById(R.id.loginBtn);
        backArrowBtn = (ImageButton) findViewById(R.id.backBtn);

        dbHelper = new DBHelper(this); // database helper obj, it consists of functions to help db operations

        // function helps in going back to signup page using back arrow button instead of mobiles's back button
        backArrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        // function to log in the user
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uname = username.getText().toString();
                String pass = password.getText().toString();

                if(uname.isEmpty() || pass.isEmpty()){
                    printToast("Please fill all the fields.");
                } else {
                    Boolean userExists = dbHelper.checkUsername(uname); // checks if user exists
                    if(userExists){
                        String hashedPassword = md5Hashing(pass);
                        Boolean loginResult = dbHelper.checkUsernameAndPassword(uname, hashedPassword); // holds the result after checking the creds
                        if(loginResult){
                            String uid = dbHelper.getUUid(uname); // gets the unique id of the user
                            Log.i("user name", uname);
                            setSession(uid, uname);
                            Intent intent = new Intent(getApplicationContext(), Passcode.class);
                            intent.putExtra("username", uname); // adding msg with intent to be accessed by next activity
                            startActivity(intent); // goes to passcode page
                        } else{
                            printToast("Invalid Credentials");
                        }
                    } else {
                        printToast("User does not exist.\n Please Sign Up.");
                    }
                }
            }
        });
    }
}