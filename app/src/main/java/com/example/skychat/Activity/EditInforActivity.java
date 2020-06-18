package com.example.skychat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.skychat.Class.Function;
import com.example.skychat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Objects;

public class EditInforActivity extends AppCompatActivity {

    private DatabaseReference userDataRef;
    private FirebaseUser currentUser;

    private RelativeLayout editinfolayout;

    private EditText editName;
    private EditText editStatus;
    private TextView editDate;
    private TextView editGender;
    private Button saveChange;
    private Toolbar editInfoToolBar;
    private int position;
    final String[] item = {"None", "Male", "Female"};
    final Calendar cal = Calendar.getInstance();
    int dYear = cal.get(Calendar.YEAR);
    int dMonth = cal.get(Calendar.MONTH);
    int dDay = cal.get(Calendar.DAY_OF_MONTH);
    private DatePickerDialog datePickerDialog;
    String datebirth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_infor);

        editName = findViewById(R.id.editNameInput);
        editStatus = findViewById(R.id.editStatusInput);
        editDate = findViewById(R.id.editdateofbirthinput);
        editGender = findViewById(R.id.editgenderselect);
        saveChange = findViewById(R.id.savechangebtn);
        editinfolayout = findViewById(R.id.editinforlayout);
        editInfoToolBar = findViewById(R.id.editinfoappbar);

        editinfolayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the input method manager
                InputMethodManager inputMethodManager = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                // Hide the soft keyboard
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(),0);
            }
        });
        setSupportActionBar(editInfoToolBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Change Your Information:");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserUid = Objects.requireNonNull(currentUser).getUid();
        userDataRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserUid);

        userDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String uName = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                String uStatus = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();
                String uGender = Objects.requireNonNull(dataSnapshot.child("gender").getValue()).toString();
                String uDate = Objects.requireNonNull(dataSnapshot.child("dateofbirth").getValue()).toString();

                editDate.setText(uDate);
                editGender.setText(uGender);
                editName.setText(uName);
                editStatus.setText(uStatus);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        editGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditInforActivity.this);
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
                        editGender.setText(item[position]);
                    }
                });
                builder.show();
            }
        });

        editDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog = new DatePickerDialog(EditInforActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int datemonth, int dayOfMonth) {
                        datebirth = dayOfMonth + "/" + (datemonth+1) + "/" + year;
                        editDate.setText(datebirth);
                    }
                }, dYear, dMonth, dDay);
                datePickerDialog.show();
            }
        });

        saveChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(EditInforActivity.this);
                progressDialog.setTitle("Saving change");
                progressDialog.setMessage("Please wait...");
                progressDialog.show();

                final String changeStatus = editStatus.getText().toString();
                final String changeName = editName.getText().toString();
                final String changeDate = editDate.getText().toString();
                final String changeGender = editGender.getText().toString();

                if (!changeStatus.equals("") && !changeName.equals("") && !changeDate.equals("") && !changeGender.equals("")){
                    userDataRef.child("status").setValue(changeStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                userDataRef.child("gender").setValue(changeGender).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            userDataRef.child("name").setValue(changeName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        userDataRef.child("dateofbirth").setValue(changeDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    progressDialog.dismiss();
                                                                    updateComplete();
                                                                } else {
                                                                    updateFalse();
                                                                }

                                                            }
                                                        });
                                                    } else {
                                                        updateFalse();
                                                    }
                                                }
                                            });
                                        } else {
                                            updateFalse();
                                        }
                                    }
                                });
                            } else {
                                updateFalse();
                            }
                        }
                    });
                } else {
                    updateFalse();
                }
            }
        });
    }

    private void updateFalse(){
        Toast.makeText(EditInforActivity.this, "Save change false!", Toast.LENGTH_LONG).show();
    }

    private void updateComplete(){
        Toast.makeText(EditInforActivity.this, "Save change success!", Toast.LENGTH_LONG).show();
    }

}
