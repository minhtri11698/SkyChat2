package com.example.skychat.Activity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.skychat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private EditText uEmail;
    private EditText uName;
    private EditText uPwd;
    private Button uForgot;
    private FirebaseAuth mAuth;
    private ProgressDialog sSignProg;
    private DatabaseReference userDatabase;
    private TextView uDateOfBirth;
    private DatePickerDialog datePickerDialog;
    private TextView genderSelect;
    private int position = 0;
    private String chosenItem;
    final Calendar cal = Calendar.getInstance();
    int dYear = cal.get(Calendar.YEAR);
    int dMonth = cal.get(Calendar.MONTH);
    int dDay = cal.get(Calendar.DAY_OF_MONTH);
    String datebirth = "01/01/1970";
    private RelativeLayout signuplayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        // Link variables with view
        uDateOfBirth = findViewById(R.id.dateofbirthinput);
        uEmail = findViewById(R.id.signemailinput);
        uName = findViewById(R.id.usernameinput);
        uPwd = findViewById(R.id.pwdinput);
        genderSelect = findViewById(R.id.genderselect);
        Button signBtn = findViewById(R.id.createbtn);
        Button uSignIn = findViewById(R.id.signinfromsignupbtn);
        Toolbar sToolBar = findViewById(R.id.signup_tool_bar);
        signuplayout = findViewById(R.id.signuplayout);
        uDateOfBirth.setText(datebirth);
        signuplayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the input method manager
                InputMethodManager inputMethodManager = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                // Hide the soft keyboard
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(),0);
            }
        });

        // Set action bar
        setSupportActionBar(sToolBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Create new account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sSignProg = new ProgressDialog(this);

        signBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get data from input
                String userEmail = uEmail.getText().toString();
                String userName = uName.getText().toString();
                String userPassword = uPwd.getText().toString();

                // Check input data
                if(userEmail.isEmpty() || userName.isEmpty() || userPassword.isEmpty()){
                    Toast.makeText(SignUpActivity.this, "Please fills in all fields", Toast.LENGTH_LONG).show();
                } else if (userPassword.length() < 6){
                    Toast.makeText(SignUpActivity.this, "Password must have at least 6 characters", Toast.LENGTH_LONG).show();
                } else {
                    sSignProg.setTitle("Creating your account!");
                    sSignProg.setMessage("Please wait while we creating your account!");
                    sSignProg.setCanceledOnTouchOutside(false);
                    sSignProg.show();
                    createNewUser(userEmail, userName, userPassword);
                }
            }
        });

        // Already have an account and move to sign in activity
        uSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent moveToLoginIntent = new Intent(SignUpActivity.this, LogInActivity.class);
                startActivity(moveToLoginIntent);
                finish();
            }
        });

        genderSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                final String[] item = {"None", "Male", "Female"};
                builder.setTitle("Select gender:");
                builder.setSingleChoiceItems(item, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        position = which;
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chosenItem = item[position];
                        genderSelect.setText(item[position]);
                    }
                });
                builder.show();
            }
        });

        uDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog = new DatePickerDialog(SignUpActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int datemonth, int dayOfMonth) {
                        datebirth = dayOfMonth + "/" + (datemonth+1) + "/" + year;
                        uDateOfBirth.setText(datebirth);
                    }
                }, dYear, dMonth, dDay);
                datePickerDialog.show();
            }
        });
    }

    // Create new user
    private void createNewUser(String userMail, final String uName, String uPwd) {
        mAuth.createUserWithEmailAndPassword(userMail, uPwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    // Get current user and create default data
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = Objects.requireNonNull(currentUser).getUid();
                    userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                    HashMap<String, String> userDataMap = new HashMap<>();
                    userDataMap.put("name", uName);
                    userDataMap.put("status", "Hi there! I'm " + uName + ".");
                    userDataMap.put("avatar", "default");
                    userDataMap.put("gender", chosenItem);
                    userDataMap.put("dateofbirth", datebirth);

                    // Set value and check if successful
                    userDatabase.setValue(userDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                // If success finish and move to main activity
                                sSignProg.dismiss();
                                Intent toMainActivity = new Intent(SignUpActivity.this, MainActivity.class);
                                toMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(toMainActivity);
                                finish();
                            }
                        }
                    });
                } else {
                    // Create new user failed
                    sSignProg.hide();
                    Toast.makeText(SignUpActivity.this, "Create account failed! Please check all the fields", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
