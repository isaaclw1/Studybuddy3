package com.example.studybuddy3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy3.datatype.StudyGroup;
import com.example.studybuddy3.datatype.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class GroupActivity extends AppCompatActivity {

    private TextView groupNameText;
    private RecyclerView memberRecyclerView;
    private MaterialButton chatButton, calendarButton, addSessionButton;

    private DatabaseReference mDatabase;
    private String groupId;
    private String userId;
    private StudyGroup currentGroup;
    private MemberAdapter memberAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        // Get groupId from intent
        groupId = getIntent().getStringExtra("groupId");
        if (groupId == null) {
            Toast.makeText(this, "Error loading group", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase and get userId
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = getSharedPreferences("StudyBuddy", MODE_PRIVATE)
                .getString("userId", null);

        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        loadGroupData();
    }

    private void initializeViews() {
        groupNameText = findViewById(R.id.groupNameText);
        memberRecyclerView = findViewById(R.id.memberRecyclerView);
        chatButton = findViewById(R.id.chatButton);
        calendarButton = findViewById(R.id.calendarButton);
        addSessionButton = findViewById(R.id.addSessionButton);
    }

    private void setupRecyclerView() {
        memberAdapter = new MemberAdapter();
        memberRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        memberRecyclerView.setAdapter(memberAdapter);
    }

    private void setupClickListeners() {
        chatButton.setOnClickListener(v -> {
            Intent intent = new Intent(GroupActivity.this, ChatActivity.class);
            intent.putExtra("groupId", groupId);
            intent.putExtra("groupName", currentGroup.getGroupName());
            startActivity(intent);
        });

        calendarButton.setOnClickListener(v -> {
            Intent intent = new Intent(GroupActivity.this, CalendarActivity.class);
            intent.putExtra("groupId", groupId);
            intent.putExtra("groupName", currentGroup.getGroupName());
            startActivity(intent);
        });

        addSessionButton.setOnClickListener(v -> {
            Intent intent = new Intent(GroupActivity.this, AddSessionActivity.class);
            intent.putExtra("groupId", groupId);
            intent.putExtra("groupName", currentGroup.getGroupName());
            startActivity(intent);
        });
    }

    private void loadGroupData() {
        mDatabase.child("groups").child(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentGroup = snapshot.getValue(StudyGroup.class);
                        if (currentGroup != null) {
                            updateUI();
                            loadMemberDetails();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(GroupActivity.this,
                                "Failed to load group details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI() {
        groupNameText.setText(currentGroup.getGroupName());
    }

    private void loadMemberDetails() {
        List<MemberData> memberDataList = new ArrayList<>();
        AtomicInteger membersLoaded = new AtomicInteger(0);

        for (String memberId : currentGroup.getMemberIds()) {
            mDatabase.child("users").child(memberId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                memberDataList.add(new MemberData(user.getUserId(), user.getEmail()));
                            }

                            if (membersLoaded.incrementAndGet() == currentGroup.getMemberIds().size()) {
                                memberAdapter.setMembers(memberDataList);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(GroupActivity.this,
                                    "Failed to load member details", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Member data class
    private static class MemberData {
        String id;
        String email;

        MemberData(String id, String email) {
            this.id = id;
            this.email = email;
        }
    }

    // RecyclerView Adapter
    private static class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {
        private List<MemberData> members = new ArrayList<>();

        public void setMembers(List<MemberData> members) {
            this.members = members;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_member, parent, false);
            return new MemberViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
            MemberData member = members.get(position);
            holder.memberNameText.setText(member.email);
        }

        @Override
        public int getItemCount() {
            return members.size();
        }

        static class MemberViewHolder extends RecyclerView.ViewHolder {
            TextView memberNameText;

            MemberViewHolder(@NonNull View itemView) {
                super(itemView);
                memberNameText = itemView.findViewById(R.id.memberNameText);
            }
        }
    }
}