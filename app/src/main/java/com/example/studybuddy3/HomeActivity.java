package com.example.studybuddy3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    private TextView userNameText;
    private MaterialButton myGroupsButton;
    private MaterialButton logoutButton;
    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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
        setupClickListeners();
        loadUserData();
    }

    private void initializeViews() {
        userNameText = findViewById(R.id.userNameText);
        myGroupsButton = findViewById(R.id.myGroupsButton);
        logoutButton = findViewById(R.id.logoutButton);
    }

    private void setupClickListeners() {
        myGroupsButton.setOnClickListener(v -> {
            // Navigate to UserGroupActivity
            startActivity(new Intent(HomeActivity.this, UserGroupActivity.class));
        });

        logoutButton.setOnClickListener(v -> logout());
    }

    private void loadUserData() {
        mDatabase.child("users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            // Extract username from email (everything before @)
                            String username = user.getEmail().split("@")[0];
                            userNameText.setText(username);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeActivity.this,
                                "Failed to load user data: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void logout() {
        // Clear shared preferences
        SharedPreferences.Editor editor = getSharedPreferences("StudyBuddy", MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        // Return to main activity
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}