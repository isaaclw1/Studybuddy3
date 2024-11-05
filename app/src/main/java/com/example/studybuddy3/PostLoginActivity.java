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

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PostLoginActivity extends AppCompatActivity {

    private RecyclerView coursesRecyclerView;
    private MaterialButton homeButton, logoutButton;
    private DatabaseReference mDatabase;
    private CourseAdapter courseAdapter;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_login);

        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Get userId from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("StudyBuddy", MODE_PRIVATE);
        userId = prefs.getString("userId", null);

        if (userId == null) {
            // If userId is not found, return to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initializeViews();
        setupRecyclerView();
        loadEnrolledCourses();
        setupClickListeners();
    }

    private void initializeViews() {
        coursesRecyclerView = findViewById(R.id.coursesRecyclerView);
        homeButton = findViewById(R.id.homeButton);
        logoutButton = findViewById(R.id.logoutButton);
    }

    private void setupRecyclerView() {
        courseAdapter = new CourseAdapter();
        coursesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        coursesRecyclerView.setAdapter(courseAdapter);
    }

    private void setupClickListeners() {
        homeButton.setOnClickListener(v -> {
            startActivity(new Intent(PostLoginActivity.this, HomeActivity.class));
        });

        logoutButton.setOnClickListener(v -> logout());
    }

    private void loadEnrolledCourses() {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null && user.getEnrolledCourses() != null) {
                    loadCourseDetails(user.getEnrolledCourses());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostLoginActivity.this,
                        "Failed to load courses: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCourseDetails(List<String> courseIds) {
        List<Course> courses = new ArrayList<>();
        for (String courseId : courseIds) {
            mDatabase.child("courses").child(courseId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Course course = snapshot.getValue(Course.class);
                            if (course != null) {
                                courses.add(course);
                                courseAdapter.setCourses(courses);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(PostLoginActivity.this,
                                    "Failed to load course details",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void logout() {
        // Clear shared preferences
        SharedPreferences.Editor editor = getSharedPreferences("StudyBuddy", MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        // Return to login screen
        Intent intent = new Intent(PostLoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // RecyclerView Adapter
    private static class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {
        private List<Course> courses = new ArrayList<>();

        public void setCourses(List<Course> courses) {
            this.courses = courses;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_course, parent, false);
            return new CourseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
            Course course = courses.get(position);
            holder.courseNameText.setText(course.getCourseName());
        }

        @Override
        public int getItemCount() {
            return courses.size();
        }

        static class CourseViewHolder extends RecyclerView.ViewHolder {
            TextView courseNameText;

            CourseViewHolder(@NonNull View itemView) {
                super(itemView);
                courseNameText = itemView.findViewById(R.id.courseNameText);
            }
        }
    }
}