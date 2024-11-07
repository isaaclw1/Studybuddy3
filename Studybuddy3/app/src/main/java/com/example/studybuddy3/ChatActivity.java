package com.example.studybuddy3;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy3.datatype.ChatMessage;
import com.example.studybuddy3.datatype.StudyGroup;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import android.widget.TextView;


public class ChatActivity extends AppCompatActivity {
    private TextView groupNameText;
    private MaterialButton selectRecipientButton;
    private RecyclerView messagesRecyclerView;
    private TextInputEditText messageEditText;
    private MaterialButton sendButton;

    private DatabaseReference mDatabase;
    private String groupId;
    private String userId;
    private StudyGroup currentGroup;
    private String currentRecipientId = "group"; // "group" for group chat, userId for individual
    private MessageAdapter messageAdapter;
    private String userEmail; // Current user's email

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        groupId = getIntent().getStringExtra("groupId");
        if (groupId == null) {
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = getSharedPreferences("StudyBuddy", MODE_PRIVATE)
                .getString("userId", null);

        initializeViews();
        setupRecyclerView();
        loadGroupData();
        loadCurrentUserEmail();
    }

    private void initializeViews() {
        groupNameText = findViewById(R.id.groupNameText);
        selectRecipientButton = findViewById(R.id.selectRecipientButton);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        selectRecipientButton.setOnClickListener(v -> showRecipientSelectionDialog());
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(userId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);
    }

    private void loadGroupData() {
        mDatabase.child("groups").child(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentGroup = snapshot.getValue(StudyGroup.class);
                        if (currentGroup != null) {
                            groupNameText.setText(currentGroup.getGroupName());
                            loadMessages();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChatActivity.this,
                                "Failed to load group", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCurrentUserEmail() {
        mDatabase.child("users").child(userId).child("email")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userEmail = snapshot.getValue(String.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void showRecipientSelectionDialog() {
        if (currentGroup == null) return;

        List<String> memberIds = new ArrayList<>(currentGroup.getMemberIds());
        List<String> memberEmails = new ArrayList<>();
        AtomicInteger loadedCount = new AtomicInteger(0);

        for (String memberId : memberIds) {
            if (!memberId.equals(userId)) {
                mDatabase.child("users").child(memberId).child("email")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String email = snapshot.getValue(String.class);
                                if (email != null) {
                                    memberEmails.add(email);
                                }

                                if (loadedCount.incrementAndGet() == memberIds.size() - 1) {
                                    showSelectionDialog(memberIds, memberEmails);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
            }
        }
    }

    private void showSelectionDialog(List<String> memberIds, List<String> memberEmails) {
        String[] options = new String[memberEmails.size() + 1];
        options[0] = "Group Chat";
        System.arraycopy(memberEmails.toArray(new String[0]), 0, options, 1, memberEmails.size());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Recipient")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        currentRecipientId = "group";
                        selectRecipientButton.setText("Select Recipient (Currently: Group Chat)");
                    } else {
                        currentRecipientId = memberIds.get(which - 1);
                        selectRecipientButton.setText("Select Recipient (Currently: " + options[which] + ")");
                    }
                    loadMessages();
                })
                .show();
    }

    private void loadMessages() {
        if (currentRecipientId.equals("group")) {
            // Loading group chat messages
            mDatabase.child("groupChats").child(groupId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            processChatMessages(snapshot);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(ChatActivity.this,
                                    "Failed to load messages", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Loading private messages between current user and selected recipient
            String chatKey = getChatKey(userId, currentRecipientId); // Unique key for the chat
            DatabaseReference privateChatsRef = mDatabase.child("privateChats").child(groupId).child(chatKey);
            privateChatsRef.orderByChild("timestamp")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            List<ChatMessage> messages = new ArrayList<>();
                            for (DataSnapshot messageSnap : snapshot.getChildren()) {
                                ChatMessage message = messageSnap.getValue(ChatMessage.class);
                                if (message != null) {
                                    messages.add(message);
                                }
                            }
                            messageAdapter.setMessages(messages);
                            if (!messages.isEmpty()) {
                                messagesRecyclerView.scrollToPosition(messages.size() - 1);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(ChatActivity.this,
                                    "Failed to load messages", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    private void processChatMessages(@NonNull DataSnapshot snapshot) {
        List<ChatMessage> messages = new ArrayList<>();
        for (DataSnapshot messageSnap : snapshot.getChildren()) {
            ChatMessage message = messageSnap.getValue(ChatMessage.class);
            if (message != null) {
                messages.add(message);
            }
        }
        messageAdapter.setMessages(messages);
        if (!messages.isEmpty()) {
            messagesRecyclerView.scrollToPosition(messages.size() - 1);
        }
    }

    private String getChatKey(String user1Id, String user2Id) {
        // Consistently create the same key regardless of who is sender/recipient
        return user1Id.compareTo(user2Id) < 0
                ? user1Id + "_" + user2Id
                : user2Id + "_" + user1Id;
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (messageText.isEmpty()) return;

        String messageId;
        DatabaseReference messagesRef;

        if (currentRecipientId.equals("group")) {
            // Sending a group message
            messagesRef = mDatabase.child("groupChats").child(groupId);
            messageId = messagesRef.push().getKey();
            if (messageId == null) return;

            ChatMessage message = new ChatMessage(
                    messageId,
                    userId,
                    "group", // Receiver ID is "group" for group messages
                    userEmail,
                    messageText,
                    System.currentTimeMillis()
            );

            messagesRef.child(messageId).setValue(message)
                    .addOnSuccessListener(aVoid -> {
                        messageEditText.setText(""); // Clear input field on success
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(ChatActivity.this,
                                    "Failed to send message", Toast.LENGTH_SHORT).show()
                    );
        } else {
            // Sending a private message
            String chatKey = getChatKey(userId, currentRecipientId); // Unique key for the chat
            messagesRef = mDatabase.child("privateChats").child(groupId).child(chatKey);
            messageId = messagesRef.push().getKey();
            if (messageId == null) return;

            ChatMessage message = new ChatMessage(
                    messageId,
                    userId,
                    currentRecipientId, // Set the receiver ID to the selected recipient
                    userEmail,
                    messageText,
                    System.currentTimeMillis()
            );

            messagesRef.child(messageId).setValue(message)
                    .addOnSuccessListener(aVoid -> {
                        messageEditText.setText(""); // Clear input field on success
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(ChatActivity.this,
                                    "Failed to send message", Toast.LENGTH_SHORT).show()
                    );
        }
    }


    private static class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
        private List<ChatMessage> messages = new ArrayList<>();
        private final String currentUserId;

        public MessageAdapter(String currentUserId) {
            this.currentUserId = currentUserId;
        }

        public void setMessages(List<ChatMessage> messages) {
            this.messages = messages;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message, parent, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            ChatMessage message = messages.get(position);
            holder.bind(message, message.getSenderId().equals(currentUserId));
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        static class MessageViewHolder extends RecyclerView.ViewHolder {
            TextView senderText, messageText, timeText;
            MaterialCardView messageCard;

            MessageViewHolder(@NonNull View itemView) {
                super(itemView);
                senderText = itemView.findViewById(R.id.senderText);
                messageText = itemView.findViewById(R.id.messageText);
                timeText = itemView.findViewById(R.id.timeText);
                messageCard = itemView.findViewById(R.id.messageCard);
            }

            void bind(ChatMessage message, boolean isCurrentUser) {
                senderText.setText(message.getSenderEmail());
                messageText.setText(message.getContent());
                timeText.setText(new SimpleDateFormat("MMM dd, HH:mm",
                        Locale.getDefault()).format(new Date(message.getTimestamp())));

                // Align messages right for current user, left for others
                LinearLayout.LayoutParams params =
                        (LinearLayout.LayoutParams) messageCard.getLayoutParams();
                if (isCurrentUser) {
                    params.gravity = Gravity.END;
                    messageCard.setCardBackgroundColor(
                            itemView.getContext().getColor(android.R.color.holo_blue_light));
                } else {
                    params.gravity = Gravity.START;
                    messageCard.setCardBackgroundColor(
                            itemView.getContext().getColor(android.R.color.white));
                }
                messageCard.setLayoutParams(params);
            }
        }
    }
}