package com.example.skychat.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.skychat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LogInActivity extends AppCompatActivity {

    private EditText logEmail;
    private EditText logPwd;
    private FirebaseAuth mAuth;
    private ProgressDialog loginProg;
    private RelativeLayout loginlayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        mAuth = FirebaseAuth.getInstance();

        logEmail = findViewById(R.id.loginemailinput);
        logPwd = findViewById(R.id.loginpwdinput);
        loginlayout = findViewById(R.id.loginlayout);

        loginlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the input method manager
                InputMethodManager inputMethodManager = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                // Hide the soft keyboard
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(),0);
            }
        });
        Button uLoginBtn = findViewById(R.id.userloginbtn);
        Toolbar lToolBar = findViewById(R.id.login_tool_bar);
        setSupportActionBar(lToolBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Log In Your Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loginProg = new ProgressDialog(this);

        uLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uMail = logEmail.getText().toString();
                String uPwd = logPwd.getText().toString();
                if(uMail.isEmpty() || uPwd.isEmpty()){
                    Toast.makeText(LogInActivity.this, "Please fills in all fields", Toast.LENGTH_LONG).show();
                } else {
                    loginProg.setTitle("Logging In...");
                    loginProg.setMessage("Logging in your account, please wait...");
                    loginProg.setCanceledOnTouchOutside(false);
                    loginProg.show();
                    loginUser(uMail, uPwd);
                }
            }
        });

    }

    private void loginUser(String uEmail, String pwd){
        mAuth.signInWithEmailAndPassword(uEmail, pwd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loginProg.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent logedInIntent = new Intent(LogInActivity.this, MainActivity.class);
                            logedInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(logedInIntent);
                            finish();
                        } else {
                            loginProg.hide();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LogInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
