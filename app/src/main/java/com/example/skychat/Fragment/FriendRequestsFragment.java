package com.example.skychat.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.skychat.Activity.MainActivity;
import com.example.skychat.Activity.UserProfileActivity;
import com.example.skychat.Model.Friend;
import com.example.skychat.Model.FriendRequest;
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

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendRequestsFragment extends Fragment {

    private RecyclerView friendrequestrecycler;
    private View friendrequestView;
    FirebaseAuth userAuth;
    private String currentUserUid;

    DatabaseReference userDatabase;
    DatabaseReference friendrequestDatabase;
    private FirebaseRecyclerAdapter<FriendRequest, FriendRequestViewHolder> friendFirebaseRecyclerAdapter;

    public FriendRequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        userAuth = FirebaseAuth.getInstance();
        currentUserUid = userAuth.getCurrentUser().getUid();
        friendrequestDatabase = FirebaseDatabase.getInstance().getReference();
        friendrequestDatabase.keepSynced(true);
        Query query = friendrequestDatabase.child("Friend_requets").child(currentUserUid).child("received");
        FirebaseRecyclerOptions<FriendRequest> friendrqFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<FriendRequest>().setQuery(query, FriendRequest.class).build();
        friendrequestView = inflater.inflate(R.layout.fragment_friend_requests, container, false);
        friendrequestrecycler = friendrequestView.findViewById(R.id.friendrequetsfrag);
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        friendrequestrecycler.setHasFixedSize(true);
        friendrequestrecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        friendFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FriendRequest, FriendRequestViewHolder>(friendrqFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendRequestViewHolder holder, int position, @NonNull FriendRequest model) {
                final String uid = getRef(position).getKey();
                friendrequestDatabase.child("Friend_requets").child(currentUserUid).child("received").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            userDatabase.child(uid).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                                    String friendrqName = dataSnapshot2.child("name").getValue().toString();
                                    String friendrqStatus = dataSnapshot2.child("status").getValue().toString();
                                    String friendrqAvatar = dataSnapshot2.child("avatar").getValue().toString();
                                    holder.userName.setText(friendrqName);
                                    holder.userStatus.setText(friendrqStatus);
                                    if (friendrqAvatar.equals("default")){
                                        Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/skychat-846b5.appspot.com/o/profile_photo%2Favatardefault.png?alt=media&token=74c3ccb9-38c2-401e-82d1-b294e730cb35").into(holder.userAvatar);
                                    } else {
                                        Picasso.get().load(friendrqAvatar).into(holder.userAvatar);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent movetoProfileIntent = new Intent(getContext(), UserProfileActivity.class);
                                    movetoProfileIntent.putExtra("userID", uid);
                                    startActivity(movetoProfileIntent);
                                }
                            });
                        }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_recycler_view_item, parent, false);
                return new FriendRequestViewHolder(view);
            }
        };
        friendrequestrecycler.setAdapter(friendFirebaseRecyclerAdapter);
        return friendrequestView;
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

    public class FriendRequestViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView userAvatar;
        public FriendRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.alluserdisplayname);
            userStatus = itemView.findViewById(R.id.alluserabout);
            userAvatar = itemView.findViewById(R.id.alluserprofilepic);
        }
    }
}
