package com.example.studybuddy3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Firebase imports
import com.example.studybuddy3.datatype.StudyGroup;
import com.example.studybuddy3.loginsignup.LoginActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// Google Material imports
import com.google.android.material.button.MaterialButton;

// Java utility imports
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class UserGroupActivity extends AppCompatActivity {
    private RecyclerView groupsRecyclerView;
    private ImageButton addGroupButton;
    private DatabaseReference mDatabase;
    private String userId;
    private GroupAdapter groupAdapter;
    private List<StudyGroup> allGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_groups);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        SharedPreferences prefs = getSharedPreferences("StudyBuddy", MODE_PRIVATE);
        userId = prefs.getString("userId", null);

        if (userId == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initializeViews();
        setupRecyclerView();
        loadUserGroups();
    }

    private void initializeViews() {
        groupsRecyclerView = findViewById(R.id.groupsRecyclerView);
        addGroupButton = findViewById(R.id.addGroupButton);

        addGroupButton.setOnClickListener(v -> {
            startActivity(new Intent(UserGroupActivity.this, AddGroupActivity.class));
        });
    }

    private void setupRecyclerView() {
        allGroups = new ArrayList<>();
        groupAdapter = new GroupAdapter(this, allGroups, userId, this::joinGroup);
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupsRecyclerView.setAdapter(groupAdapter);
    }

    private void loadUserGroups() {
        mDatabase.child("users").child(userId).child("enrolledCourses")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> enrolledCourses = new ArrayList<>();
                        for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                            String courseId = courseSnapshot.getValue(String.class);
                            if (courseId != null) {
                                enrolledCourses.add(courseId);
                            }
                        }
                        loadAllGroupsForCourses(enrolledCourses);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UserGroupActivity.this,
                                "Failed to load courses", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadAllGroupsForCourses(List<String> courseIds) {
        allGroups.clear();
        for (String courseId : courseIds) {
            mDatabase.child("courses").child(courseId).child("groupIds")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                                // Get the groupId from the key instead of the value
                                String groupId = groupSnapshot.getKey();
                                if (groupId != null) {
                                    loadGroupDetails(groupId);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(UserGroupActivity.this,
                                    "Failed to load groups", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void loadGroupDetails(String groupId) {
        mDatabase.child("groups").child(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        StudyGroup group = snapshot.getValue(StudyGroup.class);
                        if (group != null) {
                            updateGroupsList(group);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UserGroupActivity.this,
                                "Failed to load group details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateGroupsList(StudyGroup newGroup) {
        int existingIndex = -1;
        for (int i = 0; i < allGroups.size(); i++) {
            if (allGroups.get(i).getGroupId().equals(newGroup.getGroupId())) {
                existingIndex = i;
                break;
            }
        }

        if (existingIndex >= 0) {
            allGroups.set(existingIndex, newGroup);
        } else {
            allGroups.add(newGroup);
        }
        groupAdapter.notifyDataSetChanged();
    }

    private void joinGroup(StudyGroup group) {
        if (!group.getMemberIds().contains(userId)) {
            group.getMemberIds().add(userId);
            mDatabase.child("groups").child(group.getGroupId())
                    .setValue(group)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Successfully joined group", Toast.LENGTH_SHORT).show();
                        groupAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to join group", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // GroupAdapter class
    private static class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {
        private final Context context;
        private final List<StudyGroup> groups;
        private final String currentUserId;
        private final Consumer<StudyGroup> onJoinClicked;

        public GroupAdapter(Context context, List<StudyGroup> groups, String currentUserId,
                            Consumer<StudyGroup> onJoinClicked) {
            this.context = context;
            this.groups = groups;
            this.currentUserId = currentUserId;
            this.onJoinClicked = onJoinClicked;
        }

        @NonNull
        @Override
        public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_group, parent, false);
            return new GroupViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
            StudyGroup group = groups.get(position);
            holder.groupNameText.setText(group.getGroupName());

            if (group.getMemberIds().contains(currentUserId)) {
                holder.joinButton.setVisibility(View.GONE);
            } else {
                holder.joinButton.setVisibility(View.VISIBLE);
                holder.joinButton.setOnClickListener(v -> onJoinClicked.accept(group));
            }
        }

        @Override
        public int getItemCount() {
            return groups.size();
        }

        static class GroupViewHolder extends RecyclerView.ViewHolder {
            TextView groupNameText;
            MaterialButton joinButton;

            GroupViewHolder(@NonNull View itemView) {
                super(itemView);
                groupNameText = itemView.findViewById(R.id.groupNameText);
                joinButton = itemView.findViewById(R.id.joinButton);
            }
        }
    }
}