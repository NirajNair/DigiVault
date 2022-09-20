package com.example.digivault;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    EditText username, password, confirmPassword;
    Button signInBtn, signUpBtn;
    DBHelper dbHelper;
    String userSignedIn;

    public void printToast(String text){
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    public static String md5Hashing(String input)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);
            // Convert message digest into hex value
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }
        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void setSession(String uid, String username){
        User user = new User(uid, username);
        SessionManagement sessionManagement = new SessionManagement(MainActivity.this);
        sessionManagement.saveSession(user);
    }

    public static boolean isValidPassword(String password){
        String regex = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        // Compile the ReGex
        Pattern p = Pattern.compile(regex);
        if (password == null) {
            return false;
        }
        // Pattern class contains matcher() method
        // to find matching between given password
        // and regular expression.
        Matcher m = p.matcher(password);
        // Return if the password is of correct pattern
        return m.matches();
    }

    @Override
    public void onBackPressed(){
        SessionManagement sessionManagement = new SessionManagement(MainActivity.this);
        userSignedIn = sessionManagement.getSession();
        Log.i("usersignedIn", userSignedIn);
        if(userSignedIn == ""){
            printToast("Please Login!");
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        SessionManagement sessionManagement = new SessionManagement(MainActivity.this);
        userSignedIn = sessionManagement.getSession();
        if(userSignedIn != ""){
            if(dbHelper.checkPasscodeIsNull(userSignedIn)){
                Intent intent = new Intent(getApplicationContext(), CreatePasscode.class);
                intent.putExtra("uid", userSignedIn);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(getApplicationContext(), Passcode.class);
                intent.putExtra("uid", userSignedIn);
                startActivity(intent);
                finish();
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        confirmPassword = (EditText) findViewById(R.id.confirmPassword);

        signInBtn = (Button) findViewById(R.id.signInBtn);
        signUpBtn = (Button) findViewById(R.id.signUpBtn);

        dbHelper = new DBHelper(this);

        // signup function
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uname = username.getText().toString();
                String pass = password.getText().toString();
                String cpass = confirmPassword.getText().toString();
                Log.i("user details", uname+" "+pass+" "+cpass);
                Log.i("validity", String.valueOf(isValidPassword(pass)));
                if(uname.isEmpty() || pass.isEmpty() || cpass.isEmpty()){
                    printToast("Please fill all the fields.");
                } else {
                    if(pass.equals(cpass) && isValidPassword(pass)){
                        Boolean userExists = dbHelper.checkUsername(uname);
                        if(userExists){
                            printToast("User already exists.\n Please Sign In.");
                        } else {
                            String uid = UUID.randomUUID().toString(); // creating a unique id using java inbuilt utilities function
                            String hashedPassword = md5Hashing(pass);
                            Boolean regResult = dbHelper.insertUser(uid, uname, hashedPassword); // stores the result after inserting the user in db
                            if(regResult){
                                setSession(uid, uname);
                                Intent intent = new Intent(getApplicationContext(), CreatePasscode.class); // moves to gallery page
                                intent.putExtra("uid", userSignedIn);
                                intent.putExtra("username", uname);
                                startActivity(intent);
                                finish();
                            } else{
                                printToast("Registration Failed");
                            }
                        }
                    } else{
                        printToast("Passwords did not match. Password should contain at least 8 characters, one digit, one uppercase and lowercase alphabet," +
                                " one special character.");
                    }
                }
            }
        });
        // function to go to login page
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent); // goes to sign in page
            }
        });

    }
}