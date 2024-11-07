package com.example.studybuddy3;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy3.datatype.StudyGroup;
import com.example.studybuddy3.datatype.StudySession;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity {
    private TextView groupNameText;
    private CalendarView calendarView;
    private RecyclerView sessionsRecyclerView;
    private TextView noSessionsText;

    private DatabaseReference mDatabase;
    private String groupId;
    private String userId;
    private StudyGroup currentGroup;
    private Map<String, List<StudySession>> sessionsByDate = new HashMap<>();
    private SessionAdapter sessionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        groupId = getIntent().getStringExtra("groupId");
        if (groupId == null) {
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = getSharedPreferences("StudyBuddy", MODE_PRIVATE)
                .getString("userId", null);

        initializeViews();
        setupCalendarView();
        setupRecyclerView();
        loadGroupData();
        loadStudySessions();
    }

    private void initializeViews() {
        groupNameText = findViewById(R.id.groupNameText);
        calendarView = findViewById(R.id.calendarView);
        sessionsRecyclerView = findViewById(R.id.sessionsRecyclerView);
        noSessionsText = findViewById(R.id.noSessionsText);
    }

    private void setupCalendarView() {
        // Set min date to first day of current month
        Calendar minDate = Calendar.getInstance();
        minDate.set(Calendar.DAY_OF_MONTH, 1);
        calendarView.setMinDate(minDate.getTimeInMillis());

        // Set max date to last day of next month
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.MONTH, 1);
        maxDate.set(Calendar.DAY_OF_MONTH, maxDate.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendarView.setMaxDate(maxDate.getTimeInMillis());

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            showSessionsForDate(selectedDate.getTime());
        });
    }

    private void setupRecyclerView() {
        sessionAdapter = new SessionAdapter();
        sessionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sessionsRecyclerView.setAdapter(sessionAdapter);
    }

    private void loadGroupData() {
        mDatabase.child("groups").child(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentGroup = snapshot.getValue(StudyGroup.class);
                        if (currentGroup != null) {
                            groupNameText.setText(currentGroup.getGroupName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CalendarActivity.this,
                                "Failed to load group data: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadStudySessions() {
        mDatabase.child("sessions").child(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        sessionsByDate.clear();

                        for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {
                            StudySession session = sessionSnapshot.getValue(StudySession.class);
                            if (session != null && isValidSession(session)) {
                                String dateKey = getDateKey(new Date(session.getDate()));
                                sessionsByDate.computeIfAbsent(dateKey, k -> new ArrayList<>())
                                        .add(session);
                            }
                        }

                        // Show sessions for current selected date
                        showSessionsForDate(new Date(calendarView.getDate()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CalendarActivity.this,
                                "Failed to load sessions: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isValidSession(StudySession session) {
        return session.getSessionId() != null &&
                session.getStartTime() < session.getEndTime() &&
                session.getTitle() != null &&
                !session.getTitle().isEmpty();
    }

    private String getDateKey(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
    }

    private void showSessionsForDate(Date date) {
        String dateKey = getDateKey(date);
        List<StudySession> sessions = sessionsByDate.getOrDefault(dateKey, new ArrayList<>());

        if (sessions.isEmpty()) {
            noSessionsText.setVisibility(View.VISIBLE);
            sessionsRecyclerView.setVisibility(View.GONE);
        } else {
            noSessionsText.setVisibility(View.GONE);
            sessionsRecyclerView.setVisibility(View.VISIBLE);
            sessionAdapter.setSessions(sessions);
        }
    }

    private static class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {
        private List<StudySession> sessions = new ArrayList<>();

        public void setSessions(List<StudySession> sessions) {
            this.sessions = sessions;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_calendar_session, parent, false);
            return new SessionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
            StudySession session = sessions.get(position);
            holder.bind(session);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), CalendarSessionActivity.class);
                intent.putExtra("groupId", session.getGroupId());
                intent.putExtra("sessionId", session.getSessionId());
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return sessions.size();
        }

        static class SessionViewHolder extends RecyclerView.ViewHolder {
            TextView titleText, timeText, locationText;

            SessionViewHolder(@NonNull View itemView) {
                super(itemView);
                titleText = itemView.findViewById(R.id.sessionTitleText);
                timeText = itemView.findViewById(R.id.sessionTimeText);
                locationText = itemView.findViewById(R.id.sessionLocationText);
            }

            void bind(StudySession session) {
                titleText.setText(session.getTitle());

                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String timeRange = timeFormat.format(new Date(session.getStartTime())) +
                        " - " + timeFormat.format(new Date(session.getEndTime()));
                timeText.setText(timeRange);

                locationText.setText(session.getLocation());
            }
        }
    }
}