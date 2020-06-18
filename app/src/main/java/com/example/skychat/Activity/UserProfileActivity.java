package com.example.skychat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.skychat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    private TextView userdisplayname, birthandgender, userstatus;
    private CircleImageView avatarProfile;
    private Button addfriendbtn, declinefriendbtn;
    private Toolbar addfriendtoolbar;
    private DatabaseReference databaseReference, friendDataRef, friendStateRef, notifyDataRef;
    private FirebaseUser firebaseUser;
    private int current_state; // not friend = 0; request sent = 1; request received = 2; friend = 3


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userdisplayname = findViewById(R.id.userdisplayname);
        birthandgender = findViewById(R.id.usergenderandbirth);
        userstatus = findViewById(R.id.userstatus);
        addfriendbtn = findViewById(R.id.addfriendbtn);
        addfriendtoolbar = findViewById(R.id.userprofilebar);
        avatarProfile = findViewById(R.id.profilepicview);
        declinefriendbtn = findViewById(R.id.declinefriendbtn);
        declinefriendbtn.setVisibility(View.GONE);
        addfriendbtn.setVisibility(View.VISIBLE);

        current_state = 0;

        setSupportActionBar(addfriendtoolbar);
        getSupportActionBar().setTitle("User profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final String itemUserID = getIntent().getStringExtra("userID");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(itemUserID);
        friendStateRef = FirebaseDatabase.getInstance().getReference().child("Friend_requets");
        friendDataRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        notifyDataRef = FirebaseDatabase.getInstance().getReference().child("Notification");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String disName = dataSnapshot.child("name").getValue().toString();
                String thisavatar = dataSnapshot.child("avatar").getValue().toString();
                String thisstatus = dataSnapshot.child("status").getValue().toString();
                String thisdate = dataSnapshot.child("dateofbirth").getValue().toString();
                String thisgender = dataSnapshot.child("gender").getValue().toString();
                String ageandgender = thisdate + " - " + thisgender;
                userdisplayname.setText(disName);
                birthandgender.setText(ageandgender);
                userstatus.setText(thisstatus);
                if (thisavatar.equals("default")){
                    Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/skychat-846b5.appspot.com/o/profile_photo%2Favatardefault.png?alt=media&token=74c3ccb9-38c2-401e-82d1-b294e730cb35").into(avatarProfile);
                } else {
                    Picasso.get().load(thisavatar).into(avatarProfile);
                }

                friendStateRef.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("received").hasChild(itemUserID)){
                            current_state = 2;
                            addfriendbtn.setText("Accept friend request");
                            declinefriendbtn.setVisibility(View.VISIBLE);
                        } else if (dataSnapshot.child("sent").hasChild(itemUserID)){
                            current_state = 1;
                            addfriendbtn.setText("Cancel friend request");
                        } else {
                            friendDataRef.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(itemUserID)){
                                        current_state = 3;
                                        addfriendbtn.setText("Unfriend");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        addfriendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addfriendbtn.setEnabled(false);
                if (current_state == 0){
                    friendStateRef.child(firebaseUser.getUid()).child("sent").child(itemUserID).child("requetsType").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){
                                friendStateRef.child(itemUserID).child("received").child(firebaseUser.getUid()).child("requetsType").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        HashMap<String, String> notificationData = new HashMap<>();
                                        notificationData.put("from", firebaseUser.getUid());
                                        notificationData.put("type", "request");
                                        notifyDataRef.child(itemUserID).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                current_state = 1;
                                                Toast.makeText(UserProfileActivity.this, "Success", Toast.LENGTH_LONG).show();
                                                addfriendbtn.setText("Cancel friend request");
                                            }
                                        });
                                    }
                                });
                            } else {
                                Toast.makeText(UserProfileActivity.this, "Failed", Toast.LENGTH_LONG).show();
                            }
                            addfriendbtn.setEnabled(true);
                        }
                    });

                }
                if (current_state == 1) {
                    friendStateRef.child(firebaseUser.getUid()).child("sent").child(itemUserID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendStateRef.child(itemUserID).child("received").child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(UserProfileActivity.this, "Success", Toast.LENGTH_LONG).show();
                                    current_state = 0;
                                    addfriendbtn.setEnabled(true);
                                    addfriendbtn.setText("Send friend request");
                                }
                            });
                        }
                    });
                }

                if (current_state == 2){
                    final String currentDate = DateFormat.getDateInstance().format(new Date());
                    friendDataRef.child(firebaseUser.getUid()).child(itemUserID).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendDataRef.child(itemUserID).child(firebaseUser.getUid()).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    friendStateRef.child(firebaseUser.getUid()).child("received").child(itemUserID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            friendStateRef.child(itemUserID).child("sent").child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(UserProfileActivity.this, "Success", Toast.LENGTH_LONG).show();
                                                    current_state = 3;
                                                    addfriendbtn.setEnabled(true);
                                                    addfriendbtn.setText("Unfriend");
                                                    declinefriendbtn.setVisibility(View.GONE);
                                                }
                                            });
                                        }
                                    });
                                }
                            });

                        }
                    });
                }

                if (current_state == 3){
                    friendDataRef.child(firebaseUser.getUid()).child(itemUserID).child("date").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendDataRef.child(itemUserID).child(firebaseUser.getUid()).child("date").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(UserProfileActivity.this, "Success", Toast.LENGTH_LONG).show();
                                    current_state = 0;
                                    addfriendbtn.setEnabled(true);
                                    addfriendbtn.setText("Send friend request");
                                }
                            });
                        }
                    });
                }
            }
        });

        declinefriendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendStateRef.child(firebaseUser.getUid()).child("received").child(itemUserID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        friendStateRef.child(itemUserID).child("sent").child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(UserProfileActivity.this, "Success", Toast.LENGTH_LONG).show();
                                current_state = 0;
                                addfriendbtn.setEnabled(true);
                                addfriendbtn.setText("Send friend request");
                                declinefriendbtn.setVisibility(View.GONE);
                            }
                        });
                    }
                });
            }
        });
    }
}
