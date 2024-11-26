package com.example.studybuddy3;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

// Firebase imports
import com.example.studybuddy3.datatype.Course;
import com.example.studybuddy3.datatype.StudyGroup;
import com.example.studybuddy3.datatype.User;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// Google Material imports
import com.google.android.material.button.MaterialButton;

// Java utility imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AddGroupActivity extends AppCompatActivity {
    private AutoCompleteTextView courseDropdown;
    private TextInputEditText groupNameInput;
    private MaterialButton selectMembersButton;
    private MaterialButton createGroupButton;
    private ChipGroup selectedMembersChipGroup;
    private ProgressBar progressBar;

    private DatabaseReference mDatabase;
    private String userId;
    private String selectedCourseId;
    private List<String> selectedMemberIds = new ArrayList<>();
    private Map<String, String> userEmailMap = new HashMap<>(); // to store user email for display

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = getSharedPreferences("StudyBuddy", MODE_PRIVATE)
                .getString("userId", null);

        if (userId == null) {
            finish();
            return;
        }

        initializeViews();
        loadUserCourses();
        selectedMemberIds.add(userId); // Add current user as a member
    }

    private void initializeViews() {
        courseDropdown = findViewById(R.id.courseDropdown);
        groupNameInput = findViewById(R.id.groupNameInput);
        selectMembersButton = findViewById(R.id.selectMembersButton);
        createGroupButton = findViewById(R.id.createGroupButton);
        selectedMembersChipGroup = findViewById(R.id.selectedMembersChipGroup);
        progressBar = findViewById(R.id.progressBar);

        selectMembersButton.setOnClickListener(v -> showMemberSelectionDialog());
        createGroupButton.setOnClickListener(v -> validateAndCreateGroup());
    }

    private void loadUserCourses() {
        mDatabase.child("users").child(userId).child("enrolledCourses")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> courseIds = new ArrayList<>();
                        for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                            String courseId = courseSnapshot.getValue(String.class);
                            if (courseId != null) {
                                courseIds.add(courseId);
                            }
                        }
                        loadCourseDetails(courseIds);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddGroupActivity.this,
                                "Failed to load courses", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCourseDetails(List<String> courseIds) {
        Map<String, String> courseItems = new HashMap<>();
        AtomicInteger coursesLoaded = new AtomicInteger(0);

        for (String courseId : courseIds) {
            mDatabase.child("courses").child(courseId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Course course = snapshot.getValue(Course.class);
                            if (course != null) {
                                courseItems.put(course.getCourseCode(), course.getCourseId());
                            }

                            if (coursesLoaded.incrementAndGet() == courseIds.size()) {
                                setupCourseDropdown(courseItems);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(AddGroupActivity.this,
                                    "Failed to load course details", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void setupCourseDropdown(Map<String, String> courseItems) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(courseItems.keySet()));
        courseDropdown.setAdapter(adapter);

        courseDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCourseCode = (String) parent.getItemAtPosition(position);
            selectedCourseId = courseItems.get(selectedCourseCode);
            // Clear previous member selections when course changes
            selectedMemberIds.clear();
            selectedMemberIds.add(userId); // Add back the current user
            selectedMembersChipGroup.removeAllViews();
            addCurrentUserChip();
        });
    }

    private void addCurrentUserChip() {
        mDatabase.child("users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            addMemberChip(userId, user.getEmail());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }

    private void showMemberSelectionDialog() {
        if (selectedCourseId == null) {
            Toast.makeText(this, "Please select a course first", Toast.LENGTH_SHORT).show();
            return;
        }

        mDatabase.child("courses").child(selectedCourseId).child("enrolledUserIds")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> enrolledUsers = new ArrayList<>();
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String enrolledUserId = userSnapshot.getValue(String.class);
                            if (enrolledUserId != null && !enrolledUserId.equals(userId)) {
                                enrolledUsers.add(enrolledUserId);
                            }
                        }
                        loadUserDetailsAndShowDialog(enrolledUsers);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddGroupActivity.this,
                                "Failed to load enrolled users", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUserDetailsAndShowDialog(List<String> userIds) {
        AtomicInteger usersLoaded = new AtomicInteger(0);
        Map<String, String> userNames = new HashMap<>();

        for (String userId : userIds) {
            mDatabase.child("users").child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                userNames.put(userId, user.getEmail());
                                userEmailMap.put(userId, user.getEmail());
                            }

                            if (usersLoaded.incrementAndGet() == userIds.size()) {
                                showUserSelectionDialog(userNames);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(AddGroupActivity.this,
                                    "Failed to load user details", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showUserSelectionDialog(Map<String, String> userNames) {
        String[] userEmails = userNames.values().toArray(new String[0]);
        boolean[] checkedItems = new boolean[userEmails.length];
        List<String> userIds = new ArrayList<>(userNames.keySet());

        // Pre-check selected members
        for (int i = 0; i < userIds.size(); i++) {
            checkedItems[i] = selectedMemberIds.contains(userIds.get(i));
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Members")
                .setMultiChoiceItems(userEmails, checkedItems, (dialog, which, isChecked) -> {
                    String selectedUserId = userIds.get(which);
                    if (isChecked) {
                        if (!selectedMemberIds.contains(selectedUserId)) {
                            selectedMemberIds.add(selectedUserId);
                            addMemberChip(selectedUserId, userEmails[which]);
                        }
                    } else {
                        selectedMemberIds.remove(selectedUserId);
                        removeMemberChip(selectedUserId);
                    }
                })
                .setPositiveButton("Done", null)
                .show();
    }

    private void addMemberChip(String userId, String email) {
        // Check if chip already exists
        for (int i = 0; i < selectedMembersChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) selectedMembersChipGroup.getChildAt(i);
            if (chip.getTag().equals(userId)) {
                return;
            }
        }

        Chip chip = new Chip(this);
        chip.setText(email);
        chip.setTag(userId);
        chip.setCloseIconVisible(!userId.equals(this.userId)); // Cannot remove self
        chip.setOnCloseIconClickListener(v -> {
            selectedMemberIds.remove(userId);
            selectedMembersChipGroup.removeView(chip);
        });
        selectedMembersChipGroup.addView(chip);
    }

    private void removeMemberChip(String userId) {
        for (int i = 0; i < selectedMembersChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) selectedMembersChipGroup.getChildAt(i);
            if (chip.getTag().equals(userId)) {
                selectedMembersChipGroup.removeView(chip);
                break;
            }
        }
    }

    private void validateAndCreateGroup() {
        String groupName = groupNameInput.getText().toString().trim();

        if (selectedCourseId == null) {
            Toast.makeText(this, "Please select a course", Toast.LENGTH_SHORT).show();
            return;
        }

        if (groupName.isEmpty()) {
            Toast.makeText(this, "Please enter a group name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for valid group name (no special characters except hyphen and underscore)
        if (!groupName.matches("^[a-zA-Z0-9-_]+$")) {
            Toast.makeText(this, "Group name can only contain letters, numbers, hyphens, and underscores",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        checkGroupNameUniqueness(groupName);
    }

    private void checkGroupNameUniqueness(String groupName) {
        showProgress(true);
        mDatabase.child("courses").child(selectedCourseId).child("groups")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isUnique = true;
                        for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                            StudyGroup existingGroup = groupSnapshot.getValue(StudyGroup.class);
                            if (existingGroup != null &&
                                    existingGroup.getGroupName().equals(selectedCourseId + "-" + groupName)) {
                                isUnique = false;
                                break;
                            }
                        }

                        if (isUnique) {
                            createNewGroup(groupName);
                        } else {
                            showProgress(false);
                            Toast.makeText(AddGroupActivity.this,
                                    "Group name already exists for this course", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showProgress(false);
                        Toast.makeText(AddGroupActivity.this,
                                "Failed to check group name", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createNewGroup(String groupName) {
        String groupId = mDatabase.child("groups").push().getKey();
        if (groupId == null) {
            showProgress(false);
            Toast.makeText(this, "Failed to create group", Toast.LENGTH_SHORT).show();
            return;
        }

        StudyGroup newGroup = new StudyGroup();
        newGroup.setGroupId(groupId);
        newGroup.setGroupName(selectedCourseId + "-" + groupName);
        newGroup.setCourseId(selectedCourseId);
        newGroup.setMemberIds(new ArrayList<>(selectedMemberIds));
        newGroup.setCreatedAt(System.currentTimeMillis());

        Map<String, Object> updates = new HashMap<>();
        updates.put("/groups/" + groupId, newGroup);
        // Store the groupId as a string instead of boolean
        updates.put("/courses/" + selectedCourseId + "/groupIds/" + groupId, groupId);

        mDatabase.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    showProgress(false);
                    Toast.makeText(AddGroupActivity.this,
                            "Group created successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Toast.makeText(AddGroupActivity.this,
                            "Failed to create group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        courseDropdown.setEnabled(!show);
        groupNameInput.setEnabled(!show);
        selectMembersButton.setEnabled(!show);
        createGroupButton.setEnabled(!show);
    }
}