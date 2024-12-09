package com.example.studybuddy3;

import com.example.studybuddy3.datatype.StudySession;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studybuddy3.datatype.StudyGroup;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddSessionActivity extends AppCompatActivity {
    private TextInputEditText titleInput, dateInput, startTimeInput, endTimeInput, locationInput;
    private MaterialButton selectMembersButton, createSessionButton;
    private ChipGroup selectedMembersChipGroup;

    private DatabaseReference mDatabase;
    private String groupId;
    private String userId;
    private StudyGroup currentGroup;
    private Calendar selectedDate = Calendar.getInstance();
    private Calendar startTime = Calendar.getInstance();
    private Calendar endTime = Calendar.getInstance();
    private List<String> selectedAttendees = new ArrayList<>();
    private Map<String, String> userEmailMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_session);

        groupId = getIntent().getStringExtra("groupId");
        if (groupId == null) {
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = getSharedPreferences("StudyBuddy", MODE_PRIVATE)
                .getString("userId", null);

        initializeViews();
        setupClickListeners();
        loadGroupData();
    }

    private void initializeViews() {
        titleInput = findViewById(R.id.titleInput);
        dateInput = findViewById(R.id.dateInput);
        startTimeInput = findViewById(R.id.startTimeInput);
        endTimeInput = findViewById(R.id.endTimeInput);
        locationInput = findViewById(R.id.locationInput);
        selectMembersButton = findViewById(R.id.selectMembersButton);
        createSessionButton = findViewById(R.id.createSessionButton);
        selectedMembersChipGroup = findViewById(R.id.selectedMembersChipGroup);
    }

    private void setupClickListeners() {
        dateInput.setOnClickListener(v -> showDatePicker());
        startTimeInput.setOnClickListener(v -> showTimePicker(true));
        endTimeInput.setOnClickListener(v -> showTimePicker(false));
        selectMembersButton.setOnClickListener(v -> showMemberSelectionDialog());
        createSessionButton.setOnClickListener(v -> validateAndCreateSession());
    }

    private void showDatePicker() {
        Calendar minDate = Calendar.getInstance();
        minDate.set(2024, Calendar.NOVEMBER, 1);
        Calendar maxDate = Calendar.getInstance();
        maxDate.set(2025, Calendar.JANUARY, 31);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    dateInput.setText(new SimpleDateFormat("MMM dd, yyyy",
                            Locale.getDefault()).format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        datePickerDialog.show();
    }

    private void showTimePicker(boolean isStartTime) {
        Calendar calendar = isStartTime ? startTime : endTime;

        new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);

                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    if (isStartTime) {
                        startTimeInput.setText(timeFormat.format(calendar.getTime()));
                    } else {
                        endTimeInput.setText(timeFormat.format(calendar.getTime()));
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        ).show();
    }

    private void loadGroupData() {
        mDatabase.child("groups").child(groupId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentGroup = snapshot.getValue(StudyGroup.class);
                        if (currentGroup != null) {
                            loadMemberEmails();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddSessionActivity.this,
                                "Failed to load group data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMemberEmails() {
        for (String memberId : currentGroup.getMemberIds()) {
            mDatabase.child("users").child(memberId).child("email")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String email = snapshot.getValue(String.class);
                            if (email != null) {
                                userEmailMap.put(memberId, email);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }
    }

    private void showMemberSelectionDialog() {
        if (currentGroup == null) return;

        String[] memberEmails = new String[currentGroup.getMemberIds().size()];
        boolean[] checkedItems = new boolean[currentGroup.getMemberIds().size()];
        List<String> memberIds = new ArrayList<>(currentGroup.getMemberIds());

        for (int i = 0; i < memberIds.size(); i++) {
            String memberId = memberIds.get(i);
            memberEmails[i] = userEmailMap.get(memberId);
            checkedItems[i] = selectedAttendees.contains(memberId);
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Members")
                .setMultiChoiceItems(memberEmails, checkedItems, (dialog, which, isChecked) -> {
                    String selectedId = memberIds.get(which);
                    if (isChecked) {
                        selectedAttendees.add(selectedId);
                        addMemberChip(selectedId, memberEmails[which]);
                    } else {
                        selectedAttendees.remove(selectedId);
                        removeMemberChip(selectedId);
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
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            selectedAttendees.remove(userId);
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

    private void validateAndCreateSession() {
        String title = titleInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();

        // Validation
        if (title.isEmpty()) {
            titleInput.setError("Please enter a title");
            return;
        }

        if (dateInput.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startTimeInput.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please select a start time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (endTimeInput.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please select an end time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (location.isEmpty()) {
            locationInput.setError("Please enter a location");
            return;
        }

        if (selectedAttendees.isEmpty()) {
            Toast.makeText(this, "Please select at least one member", Toast.LENGTH_SHORT).show();
            return;
        }

        // Combine date with times
        Calendar sessionStart = (Calendar) selectedDate.clone();
        sessionStart.set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY));
        sessionStart.set(Calendar.MINUTE, startTime.get(Calendar.MINUTE));

        Calendar sessionEnd = (Calendar) selectedDate.clone();
        sessionEnd.set(Calendar.HOUR_OF_DAY, endTime.get(Calendar.HOUR_OF_DAY));
        sessionEnd.set(Calendar.MINUTE, endTime.get(Calendar.MINUTE));

        // Validate time logic
        if (sessionEnd.before(sessionStart)) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
            return;
        }

        createSession(title, sessionStart.getTimeInMillis(),
                sessionEnd.getTimeInMillis(), location);
    }

    private void createSession(String title, long startTime, long endTime, String location) {
        String sessionId = mDatabase.child("sessions").push().getKey();
        if (sessionId == null) {
            Toast.makeText(this, "Failed to create session", Toast.LENGTH_SHORT).show();
            return;
        }

        StudySession newSession = new StudySession(
                sessionId,
                title,
                selectedDate.getTimeInMillis(),
                startTime,
                endTime,
                location,
                groupId
        );
        newSession.setAttendeeIds(new ArrayList<>(selectedAttendees));

        Map<String, Object> updates = new HashMap<>();
        updates.put("/sessions/" + groupId + "/" + sessionId, newSession);

        mDatabase.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddSessionActivity.this,
                            "Study session created successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(AddSessionActivity.this,
                                "Failed to create session: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }
}