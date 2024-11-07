package com.example.studybuddy3;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy3.datatype.StudySession;
import com.google.android.material.button.MaterialButton;
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

public class CalendarSessionActivity extends AppCompatActivity {
    private TextView sessionTitleText;
    private TextView dateText;
    private TextView timeText;
    private TextView locationText;
    private RecyclerView attendeesRecyclerView;
    private MaterialButton resourcesButton;

    private DatabaseReference mDatabase;
    private String groupId;
    private String sessionId;
    private StudySession currentSession;
    private AttendeeAdapter attendeeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_session);

        groupId = getIntent().getStringExtra("groupId");
        sessionId = getIntent().getStringExtra("sessionId");

        if (groupId == null || sessionId == null) {
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        initializeViews();
        setupRecyclerView();
        loadSessionData();
    }

    private void initializeViews() {
        sessionTitleText = findViewById(R.id.sessionTitleText);
        dateText = findViewById(R.id.dateText);
        timeText = findViewById(R.id.timeText);
        locationText = findViewById(R.id.locationText);
        attendeesRecyclerView = findViewById(R.id.attendeesRecyclerView);
        resourcesButton = findViewById(R.id.resourcesButton);

        resourcesButton.setOnClickListener(v -> {
            if (currentSession != null) {
                Intent intent = new Intent(CalendarSessionActivity.this, ResourceActivity.class);
                intent.putExtra("groupId", groupId);
                intent.putExtra("sessionId", sessionId);
                startActivity(intent);
            }
        });
    }

    private void setupRecyclerView() {
        attendeeAdapter = new AttendeeAdapter();
        attendeesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        attendeesRecyclerView.setAdapter(attendeeAdapter);
    }

    private void loadSessionData() {
        mDatabase.child("sessions").child(groupId).child(sessionId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentSession = snapshot.getValue(StudySession.class);
                        if (currentSession != null) {
                            updateUI();
                            loadAttendeeDetails();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CalendarSessionActivity.this,
                                "Failed to load session data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI() {
        sessionTitleText.setText(currentSession.getTitle());

        // Format and set date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        dateText.setText(dateFormat.format(new Date(currentSession.getDate())));

        // Format and set time
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String timeRange = timeFormat.format(new Date(currentSession.getStartTime())) +
                " - " + timeFormat.format(new Date(currentSession.getEndTime()));
        timeText.setText(timeRange);

        locationText.setText(currentSession.getLocation());
    }

    private void loadAttendeeDetails() {
        if (currentSession.getAttendeeIds() != null && !currentSession.getAttendeeIds().isEmpty()) {
            Map<String, String> attendeeEmails = new HashMap<>();
            AtomicInteger loadedCount = new AtomicInteger(0);

            for (String attendeeId : currentSession.getAttendeeIds()) {
                mDatabase.child("users").child(attendeeId).child("email")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String email = snapshot.getValue(String.class);
                                if (email != null) {
                                    attendeeEmails.put(attendeeId, email);
                                }

                                if (loadedCount.incrementAndGet() == currentSession.getAttendeeIds().size()) {
                                    attendeeAdapter.setAttendees(new ArrayList<>(attendeeEmails.values()));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(CalendarSessionActivity.this,
                                        "Failed to load attendee details", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    private class AttendeeAdapter extends RecyclerView.Adapter<AttendeeAdapter.AttendeeViewHolder> {
        private List<String> attendeeEmails = new ArrayList<>();

        public void setAttendees(List<String> attendeeEmails) {
            this.attendeeEmails = attendeeEmails;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public AttendeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_attendee, parent, false);
            return new AttendeeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AttendeeViewHolder holder, int position) {
            holder.attendeeEmailText.setText(attendeeEmails.get(position));
        }

        @Override
        public int getItemCount() {
            return attendeeEmails.size();
        }

        class AttendeeViewHolder extends RecyclerView.ViewHolder {
            TextView attendeeEmailText;

            AttendeeViewHolder(@NonNull View itemView) {
                super(itemView);
                attendeeEmailText = itemView.findViewById(R.id.attendeeEmailText);
            }
        }
    }
}