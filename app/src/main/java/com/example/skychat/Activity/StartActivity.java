package com.example.skychat.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.skychat.R;

public class StartActivity extends AppCompatActivity {

    Button signUpbtn;
    Button logInBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        signUpbtn = findViewById(R.id.signupbutton);
        logInBtn = findViewById(R.id.loginbutton);

        signUpbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(StartActivity.this, SignUpActivity.class);
                startActivity(signUpIntent);
            }
        });

        logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(StartActivity.this, LogInActivity.class);
                startActivity(loginIntent);
            }
        });
    }
}
