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

import com.example.skychat.Activity.ChatActivity;
import com.example.skychat.Activity.UserProfileActivity;
import com.example.skychat.Class.TimeAgo;
import com.example.skychat.Model.Conversation;
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


public class MessagesFragment extends Fragment {

    private RecyclerView converRecycler;
    private String converAvatar, converUserName, converTime, converPreview;
    FirebaseAuth uAuth;
    private View converView;
    private String currentUserUid;

    DatabaseReference userDatabase;
    DatabaseReference converDatabase;
    DatabaseReference messageDatabase;
    private FirebaseRecyclerAdapter<Conversation, MessagesFragment.MessageViewHolder> conversationFirebaseRecyclerAdapter;

    public MessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        converView = inflater.inflate(R.layout.fragment_messages, container,false);
        converRecycler = converView.findViewById(R.id.mainMessageView);
        uAuth = FirebaseAuth.getInstance();
        currentUserUid = uAuth.getCurrentUser().getUid();
        converDatabase = FirebaseDatabase.getInstance().getReference();
        converDatabase.keepSynced(true);
        messageDatabase = FirebaseDatabase.getInstance().getReference();
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        converRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        Query query = converDatabase.child("Chat").child(currentUserUid);
        FirebaseRecyclerOptions<Conversation> conversationFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Conversation>().setQuery(query, Conversation.class).build();
        conversationFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Conversation, MessageViewHolder>(conversationFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final MessageViewHolder holder, int position, @NonNull Conversation model) {
                final String uid = getRef(position).getKey();
                userDatabase.child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        converUserName = dataSnapshot.child("name").getValue().toString();
                        converAvatar = dataSnapshot.child("avatar").getValue().toString();
                        holder.converNameView.setText(converUserName);
                        if (converAvatar.equals("default")){
                            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/skychat-846b5.appspot.com/o/profile_photo%2Favatardefault.png?alt=media&token=74c3ccb9-38c2-401e-82d1-b294e730cb35").into(holder.converAvatarView);
                        } else {
                            Picasso.get().load(converAvatar).into(holder.converAvatarView);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                Query lastQuery = messageDatabase.child("Message").child(currentUserUid).child(uid).orderByKey().limitToLast(1);
                lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String messagecontent, messType, lastMessTime;
                        for (DataSnapshot child: dataSnapshot.getChildren()) {
                            messagecontent = child.child("message").getValue().toString();
                            lastMessTime = child.child("time").getValue().toString();
                            messType = child.child("type").getValue().toString();
                            TimeAgo timeAgo = new TimeAgo();
                            long lastMessageTime = Long.parseLong(lastMessTime);
                            String stat = timeAgo.getSendTime(lastMessageTime, getContext());
                            holder.converTimeView.setText(stat);
                            if (messType.equals("image")){
                                messagecontent = "image";
                            }
                            holder.converPreviewView.setText(messagecontent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent sendMessage = new Intent(getContext(), ChatActivity.class);
                        sendMessage.putExtra("userID", uid);
                        sendMessage.putExtra("userName", converUserName);
                        sendMessage.putExtra("useravatar", converAvatar);
                        startActivity(sendMessage);
                    }
                });
            }

            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_conver_item, parent, false);
                return new MessagesFragment.MessageViewHolder(view);
            }
        };
        converRecycler.setAdapter(conversationFirebaseRecyclerAdapter);
        return converView;

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView converNameView, converTimeView, converPreviewView;
        CircleImageView converAvatarView;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            converNameView = itemView.findViewById(R.id.converUserName);
            converTimeView = itemView.findViewById(R.id.lastMessageTime);
            converPreviewView = itemView.findViewById(R.id.lastMessagePreview);
            converAvatarView = itemView.findViewById(R.id.converAvata);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        conversationFirebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (conversationFirebaseRecyclerAdapter != null){
            conversationFirebaseRecyclerAdapter.stopListening();
        }
    }
}
