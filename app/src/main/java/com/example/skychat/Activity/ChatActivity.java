package com.example.skychat.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.PointerIconCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.skychat.Class.TimeAgo;
import com.example.skychat.Model.Message;
import com.example.skychat.Model.User;
import com.example.skychat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.security.Provider;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

// Activity chat

public class ChatActivity extends AppCompatActivity {

    private static final int GALLERY_PICK = 1;
    private String chatFUid, currentUserName, userAvatar, currentUserUid, currentSender = "";
    private TextView userNameTextView, lastOnlineView;
    private ImageButton addOnMessage, sendMessage;
    private EditText messageInput;
    private RecyclerView messageRecyclerView;
    private CircleImageView userAvataView;
    private Toolbar chatToolBar;
    private FirebaseAuth firebaseAuth;
    private RelativeLayout messageLayout;
    private StorageReference imageStorage = FirebaseStorage.getInstance().getReference();
    private DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseRecyclerAdapter<Message, RecyclerView.ViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get data send from intent
        chatFUid = getIntent().getStringExtra("userID");
        currentUserName = getIntent().getStringExtra("userName");
        userAvatar = getIntent().getStringExtra("useravatar");

        // Set custom action bar
        chatToolBar = findViewById(R.id.customchatbarlayout);
        setSupportActionBar(chatToolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(action_bar_view);

        // Link view
        userNameTextView = findViewById(R.id.chatUserName);
        lastOnlineView = findViewById(R.id.chatActive);
        userAvataView = findViewById(R.id.chatprofilepic);
        addOnMessage = findViewById(R.id.addOnMessageBtn);
        messageInput = findViewById(R.id.messageInput);
        sendMessage = findViewById(R.id.sendMessageBtn);
        messageRecyclerView = findViewById(R.id.messageItemRecycleView);
        messageLayout = findViewById(R.id.chatRelativeLayout);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(linearLayoutManager);

        // Define
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserUid = firebaseAuth.getCurrentUser().getUid();
        Query query = dataRef.child("Message").child(currentUserUid).child(chatFUid);
        FirebaseRecyclerOptions<Message> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Message>().setQuery(query, Message.class).build();

        final String currentUserRef = currentUserUid + "/" + chatFUid;
        final String receiverRef = chatFUid + "/" + currentUserUid;

        // Set view
        userNameTextView.setText(currentUserName);
        if (userAvatar.equals("default")){
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/skychat-846b5.appspot.com/o/profile_photo%2Favatardefault.png?alt=media&token=74c3ccb9-38c2-401e-82d1-b294e730cb35").into(userAvataView);
        } else {
            Picasso.get().load(userAvatar).into(userAvataView);
        }

        // Set last seen feature
        dataRef.child("Users").child(chatFUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String onlineStat = dataSnapshot.child("online").getValue().toString();
                if (onlineStat.equals("true")){
                    lastOnlineView.setText("Online");
                } else {
                    TimeAgo timeAgo = new TimeAgo();
                    long lastOnlineTime = Long.parseLong(onlineStat);
                    String stat = timeAgo.getTimeAgo(lastOnlineTime, getApplicationContext());
                    lastOnlineView.setText(stat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        dataRef.child("Conversations").child(currentUserUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(chatFUid)){
                    Map addChatMap = new HashMap();
                    addChatMap.put("Seen", false);
                    addChatMap.put("timeStamp", ServerValue.TIMESTAMP);

                    Map userConverMap = new HashMap();
                    userConverMap.put("Chat/" + currentUserRef, addChatMap);
                    userConverMap.put("Chat/" + receiverRef, addChatMap);
                     dataRef.updateChildren(userConverMap, new DatabaseReference.CompletionListener() {
                         @Override
                         public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                             if(databaseError == null){
                                 Toast.makeText(ChatActivity.this, "SUCCESS", Toast.LENGTH_LONG).show();
                             } else {
                                 Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_LONG).show();;
                             }
                         }
                     });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNewMessage();
            }
        });

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Message, RecyclerView.ViewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull Message model) {
                if (holder.getItemViewType() == 0){
                    ReceiverViewHolder receiverViewHolder = (ReceiverViewHolder) holder;
                    receiverViewHolder.setItemToReceiverView(model);
                } else {
                    SenderViewHolder senderViewHolder = (SenderViewHolder) holder;
                    senderViewHolder.setItemToSenderView(model);
                }
                messageRecyclerView.smoothScrollToPosition(position);
            }

            @Override
            public int getItemViewType(int position) {
                String uid = super.getItem(position).getFrom();
                if (currentUserUid.equals(uid)){
                    return 1;
                } else {
                    return 0;
                }
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                View view;
                if (viewType == 0){
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item_layout, parent, false);
                    return new ReceiverViewHolder(view);
                } else {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item_sender, parent, false);
                    return new SenderViewHolder(view);
                }
            }
        };

        messageRecyclerView.setAdapter(firebaseRecyclerAdapter);

        messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager chatInputMethodManager;
                chatInputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                // Hide the soft keyboard
                chatInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
            }
        });

        addOnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery_intent = new Intent();
                gallery_intent.setType("image/*");
                gallery_intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(gallery_intent, "Select image"), GALLERY_PICK);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, final Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            Uri imageuri = data.getData();

            final String currentUserRef = "Message/"  + currentUserUid + "/" + chatFUid;
            final String receiverRef = "Message/" + chatFUid + "/" + currentUserUid;
            DatabaseReference userMessagePush = dataRef.child("Message").child(currentUserUid).child(chatFUid).push();

            final String pushID = userMessagePush.getKey();

            StorageReference filePath = imageStorage.child("image_message").child(pushID + ".jpg");
            filePath.putFile(imageuri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        String downloadURL = "https://firebasestorage.googleapis.com/v0/b/skychat-846b5.appspot.com/o/image_message%2F" + pushID + ".jpg?alt=media";

                        Map messageMap = new HashMap();
                        messageMap.put("message", downloadURL);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", currentUserUid);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(currentUserRef + "/" + pushID, messageMap);
                        messageUserMap.put(receiverRef + "/" + pushID, messageMap);

                        dataRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if(databaseError == null){
                                    Toast.makeText(ChatActivity.this, "SUCCESS", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_LONG).show();;
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void sendNewMessage() {
        String message = messageInput.getText().toString();
        if (!message.isEmpty()){
            messageInput.setText(null);
            DatabaseReference userMessagePush = dataRef.child("Messages").child(currentUserUid).child(chatFUid).push();

            String pushId = userMessagePush.getKey();

            final String currentUserRef = "Message/"  + currentUserUid + "/" + chatFUid;
            final String receiverRef = "Message/" + chatFUid + "/" + currentUserUid;

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", currentUserUid);

            Map messageUserMap = new HashMap();
            messageUserMap.put(currentUserRef + "/" + pushId, messageMap);
            messageUserMap.put(receiverRef + "/" + pushId, messageMap);

            dataRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError == null){
                        Toast.makeText(ChatActivity.this, "SUCCESS", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ChatActivity.this, "ERROR", Toast.LENGTH_LONG).show();;
                    }
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (firebaseRecyclerAdapter != null){
            firebaseRecyclerAdapter.stopListening();
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {

        TextView messageContent, receivedTime;
        CircleImageView receiverAvata;
        ImageView receiveImage;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContent = itemView.findViewById(R.id.messageContentView);
            receiverAvata = itemView.findViewById(R.id.receiverAvatarView);
            receivedTime = itemView.findViewById(R.id.receivedTime);
            receiveImage = itemView.findViewById(R.id.imageMessageReceived);
        }

        void setItemToReceiverView(Message message){
            if (userAvatar.equals("default")) {
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/skychat-846b5.appspot.com/o/profile_photo%2Favatardefault.png?alt=media&token=74c3ccb9-38c2-401e-82d1-b294e730cb35").into(userAvataView);
            } else {
                Picasso.get().load(userAvatar).into(receiverAvata);
            }
            if (message.getFrom().equals(currentSender)){
                receiverAvata.setVisibility(View.INVISIBLE);
            } else {
                receiverAvata.setVisibility(View.VISIBLE);
            }
            currentSender = message.getFrom();
            String messagecontent = message.getMessage();
            if (message.getType().equals("text")){
                messageContent.setText(messagecontent);
                messageContent.setVisibility(View.VISIBLE);
                receiveImage.setVisibility(View.GONE);
            } else if (message.getType().equals("image")){
                Picasso.get().load(messagecontent).into(receiveImage);
                messageContent.setVisibility(View.GONE);
                receiveImage.setVisibility(View.VISIBLE);
            }

            TimeAgo sendtimeAgo = new TimeAgo();
            long sentTime = message.getTime();
            String sentTime1 = sendtimeAgo.getSendTime(sentTime, getApplicationContext());
            receivedTime.setText(sentTime1);
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {

        TextView messageSenderContent, sendTime;
        ImageView senderImageMess;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            messageSenderContent = itemView.findViewById(R.id.messageSenderContentView);
            sendTime = itemView.findViewById(R.id.sendTime);
            senderImageMess = itemView.findViewById(R.id.imageMessageSender);
        }

        void setItemToSenderView(Message message){
            currentSender = "";
            TimeAgo sendtimeAgo = new TimeAgo();
            long sentTime = message.getTime();
            String sentTime1 = sendtimeAgo.getSendTime(sentTime, getApplicationContext());
            sendTime.setText(sentTime1);
            String messagecontent = message.getMessage();
            if (message.getType().equals("text")){
                messageSenderContent.setText(messagecontent);
                messageSenderContent.setVisibility(View.VISIBLE);
                senderImageMess.setVisibility(View.GONE);
            } else if (message.getType().equals("image")){
                Picasso.get().load(messagecontent).into(senderImageMess);
                messageSenderContent.setVisibility(View.GONE);
                senderImageMess.setVisibility(View.VISIBLE);
            }
        }
    }
}
