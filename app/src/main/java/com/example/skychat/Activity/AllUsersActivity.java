package com.example.skychat.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.skychat.Model.User;
import com.example.skychat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {

    private Toolbar allusertoolbar;
    private RecyclerView alluserrecycler;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    Query query = databaseReference.child("Users");
    private FirebaseRecyclerAdapter<User, UsersViewHolder> firebaseRecyclerAdapter;
    FirebaseRecyclerOptions<User> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<User>().setQuery(query, User.class).build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        allusertoolbar = findViewById(R.id.allusertoolbar);
        alluserrecycler = findViewById(R.id.alluserrecyclerview);

        setSupportActionBar(allusertoolbar);
        getSupportActionBar().setTitle("All user:");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        alluserrecycler.setHasFixedSize(true);
        alluserrecycler.setLayoutManager(new LinearLayoutManager(this));

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, UsersViewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull User model) {
                holder.setItemToView(model);
                final String uid = getRef(position).getKey();
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent toProfileIntent = new Intent(AllUsersActivity.this, UserProfileActivity.class);
                        toProfileIntent.putExtra("userID", uid);
                        startActivity(toProfileIntent);
                    }
                });
            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_recycler_view_item, parent, false);
                return new UsersViewHolder(view);
            }
        };
        alluserrecycler.setAdapter(firebaseRecyclerAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (firebaseRecyclerAdapter != null){
            firebaseRecyclerAdapter.stopListening();
        }
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus, onlinestat;
        CircleImageView userAvatar;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            onlinestat = itemView.findViewById(R.id.onlinestatus);
            userName = itemView.findViewById(R.id.alluserdisplayname);
            userStatus = itemView.findViewById(R.id.alluserabout);
            userAvatar = itemView.findViewById(R.id.alluserprofilepic);
        }

        void setItemToView(User user){
            String itemUserName = user.getName();
            userName.setText(itemUserName);
            String itemUserStatus = user.getStatus();
            userStatus.setText(itemUserStatus);
            String itemUserAvatar = user.getAvatar();
            if (itemUserAvatar.equals("default")){
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/skychat-846b5.appspot.com/o/profile_photo%2Favatardefault.png?alt=media&token=74c3ccb9-38c2-401e-82d1-b294e730cb35").into(userAvatar);
            } else {
                Picasso.get().load(itemUserAvatar).into(userAvatar);
            }
            onlinestat.setVisibility(View.GONE);
        }
    }
}
