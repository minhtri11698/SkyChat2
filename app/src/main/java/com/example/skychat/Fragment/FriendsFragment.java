package com.example.skychat.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.skychat.Activity.AllUsersActivity;
import com.example.skychat.Activity.ChatActivity;
import com.example.skychat.Activity.UserProfileActivity;
import com.example.skychat.Model.Friend;
import com.example.skychat.Model.User;
import com.example.skychat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView friendrecycler;
    private String friendAvatar, friendName;
    private View friendView;
    FirebaseAuth uAuth;
    private String currentUserUid;

    DatabaseReference userDatabase;
    DatabaseReference friendDatabase;
    private FirebaseRecyclerAdapter<Friend, FriendViewHolder> friendFirebaseRecyclerAdapter;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        uAuth = FirebaseAuth.getInstance();
        currentUserUid = uAuth.getCurrentUser().getUid();
        friendDatabase = FirebaseDatabase.getInstance().getReference();
        friendDatabase.keepSynced(true);
        Query query = friendDatabase.child("Friends").child(currentUserUid);
        FirebaseRecyclerOptions<Friend> friendFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Friend>().setQuery(query, Friend.class).build();
        friendView = inflater.inflate(R.layout.fragment_friends, container, false);
        friendrecycler = friendView.findViewById(R.id.friendfragment);
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        userDatabase.keepSynced(true);
        friendrecycler.setHasFixedSize(true);
        friendrecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        friendFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friend, FriendViewHolder>(friendFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendViewHolder holder, int position, @NonNull Friend model) {
                final String uid = getRef(position).getKey();
                userDatabase.child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        friendName = dataSnapshot.child("name").getValue().toString();
                        String friendStatus = dataSnapshot.child("status").getValue().toString();
                        friendAvatar = dataSnapshot.child("avatar").getValue().toString();
                        String stat = dataSnapshot.child("online").getValue().toString();
                        holder.userName.setText(friendName);
                        holder.userStatus.setText(friendStatus);
                        if (friendAvatar.equals("default")){
                            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/skychat-846b5.appspot.com/o/profile_photo%2Favatardefault.png?alt=media&token=74c3ccb9-38c2-401e-82d1-b294e730cb35").into(holder.userAvatar);
                        } else {
                            Picasso.get().load(friendAvatar).into(holder.userAvatar);
                        }

                        if (stat.equals("true")){
                            holder.onlinestat.setText("Online");
                            holder.onlinestat.setTextColor(-16711936);
                        } else {
                            holder.onlinestat.setText("Offline");
                            holder.onlinestat.setTextColor(-65536);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String[] item = {"View friend profile", "Send message"};
                        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                        b.setTitle("Select:");
                        b.setItems(item, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    Intent movetoProfile = new Intent(getContext(), UserProfileActivity.class);
                                    movetoProfile.putExtra("userID", uid);
                                    startActivity(movetoProfile);
                                } else if (which == 1){
                                    Intent sendMessage = new Intent(getContext(), ChatActivity.class);
                                    sendMessage.putExtra("userID", uid);
                                    sendMessage.putExtra("userName", friendName);
                                    Log.d("usernamecheck", friendName);
                                    sendMessage.putExtra("useravatar", friendAvatar);
                                    startActivity(sendMessage);
                                }
                            }
                        });
                        b.show();
                    }
                });
            }

            @NonNull
            @Override
            public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_recycler_view_item, parent, false);
                return new FriendViewHolder(view);
            }
        };
        friendrecycler.setAdapter(friendFirebaseRecyclerAdapter);
        return friendView;
    }

    @Override
    public void onStart() {
        super.onStart();
        friendFirebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (friendFirebaseRecyclerAdapter != null){
            friendFirebaseRecyclerAdapter.stopListening();
        }
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus, onlinestat;
        CircleImageView userAvatar;
        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            onlinestat = itemView.findViewById(R.id.onlinestatus);
            userName = itemView.findViewById(R.id.alluserdisplayname);
            userStatus = itemView.findViewById(R.id.alluserabout);
            userAvatar = itemView.findViewById(R.id.alluserprofilepic);
        }
    }
}
