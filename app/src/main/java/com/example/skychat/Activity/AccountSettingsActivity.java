package com.example.skychat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.skychat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.EventListener;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountSettingsActivity extends AppCompatActivity {

    private DatabaseReference userDataRef;
    private FirebaseUser currentUser;

    private Boolean showBtn = false;
    private Toolbar settingstoolbar;
    private TextView usernametxtview;
    private TextView userinfortxtview;
    private CircleImageView useravatar;
    private TextView userDate;
    private TextView userGender;
    private FloatingActionButton editButton, editAvatarBtn, editPwdBtn, editInfoBtn;
    private RelativeLayout settingLayout;
    private ProgressDialog settingDialog;
    private StorageReference mStorageRef;
    FirebaseStorage storage;
    private static final int GALLERY_CHOOSE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        userDate = findViewById(R.id.userdatebirth);
        userGender = findViewById(R.id.usergendertv);
        settingstoolbar = findViewById(R.id.settingsbar);
        useravatar = findViewById(R.id.profileavatar);
        usernametxtview = findViewById(R.id.usernamesettings);
        userinfortxtview = findViewById(R.id.aboutuser);
        editButton = findViewById(R.id.floateditbtn);
        editAvatarBtn = findViewById(R.id.flteditAvatarBtn);
        editInfoBtn = findViewById(R.id.flteditInforBtn);
        editPwdBtn = findViewById(R.id.flteditPwdBtn);
        settingLayout = findViewById(R.id.settingsaccountlayout);

        setSupportActionBar(settingstoolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        hideFloatBtn();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        final String currentUserUid = currentUser.getUid();
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("profile_photo/" + currentUserUid + "_profile_pic.jpg");

        userDataRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserUid);

        userDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String uName = dataSnapshot.child("name").getValue().toString();
                String uAvatar = dataSnapshot.child("avatar").getValue().toString();
                String uStatus = dataSnapshot.child("status").getValue().toString();
                String uGender = dataSnapshot.child("gender").getValue().toString();
                String uDate = dataSnapshot.child("dateofbirth").getValue().toString();

                if (!uAvatar.equals("default") ){
                    Picasso.get().load(uAvatar).into(useravatar);
                }
                userDate.setText(uDate);
                userGender.setText(uGender);
                usernametxtview.setText(uName);
                userinfortxtview.setText(uStatus);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!showBtn){
                    showFloatBtn();
                    showBtn = true;
                } else {
                    hideFloatBtn();
                    showBtn = false;
                }
            }
        });

        settingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showBtn){
                    hideFloatBtn();
                    showBtn = false;
                }
            }
        });

        editAvatarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start picker to get image for cropping and then use the image in cropping activity
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .start(AccountSettingsActivity.this);
            }
        });

        editInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toEditInfoIntent = new Intent(AccountSettingsActivity.this, EditInforActivity.class);
                startActivity(toEditInfoIntent);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_CHOOSE && resultCode == RESULT_OK){
            Uri photoUri = data.getData();
            CropImage.activity(photoUri).start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                settingDialog = new ProgressDialog(AccountSettingsActivity.this);
                settingDialog.setTitle("Uploading your photo");
                settingDialog.setMessage("Please wait...");
                settingDialog.setCanceledOnTouchOutside(false);
                settingDialog.show();

                Uri resultUri = result.getUri();

                final StorageReference filepath = mStorageRef.child("profile_photo").child(currentUser.getUid()+"_profile_pic.jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            final String downloadUrl = "https://firebasestorage.googleapis.com/v0/b/skychat-846b5.appspot.com/o/profile_photo%2F" + currentUser.getUid() + "_profile_pic.jpg?alt=media";
                            userDataRef.child("avatar").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        settingDialog.dismiss();
                                        Toast.makeText(AccountSettingsActivity.this, "Success", Toast.LENGTH_LONG).show();
                                        Picasso.get().load(downloadUrl).into(useravatar);
                                    } else {
                                        settingDialog.dismiss();
                                        Toast.makeText(AccountSettingsActivity.this, "Upload failed", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void showFloatBtn(){
        editAvatarBtn.show();
        editInfoBtn.show();
        editPwdBtn.show();
    }

    private void hideFloatBtn(){
        editInfoBtn.hide();
        editAvatarBtn.hide();
        editPwdBtn.hide();
    }
}
