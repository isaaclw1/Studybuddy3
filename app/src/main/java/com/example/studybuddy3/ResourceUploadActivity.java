package com.example.studybuddy3;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studybuddy3.datatype.Resource;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ResourceUploadActivity extends AppCompatActivity {

    private EditText resourceNameInput;
    private EditText resourceDescriptionInput;
    private EditText resourceCategoryInput;
    private EditText resourceUrlInput;
    private DatabaseReference mDatabase;
    private String sessionId;
    private String uploaderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_upload);

        sessionId = getIntent().getStringExtra("sessionId");
        if (sessionId == null) {
            Toast.makeText(this, "No session ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Retrieve userId from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("StudyBuddy", MODE_PRIVATE);
        uploaderId = prefs.getString("userId", null);

        if (uploaderId == null) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();

        resourceNameInput = findViewById(R.id.resourceNameInput);
        resourceDescriptionInput = findViewById(R.id.resourceDescriptionInput);
        resourceCategoryInput = findViewById(R.id.resourceCategoryInput);
        resourceUrlInput = findViewById(R.id.resourceUrlInput);

        findViewById(R.id.uploadResourceButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadResource();
            }
        });
    }

    private void uploadResource() {
        String name = resourceNameInput.getText().toString().trim();
        String description = resourceDescriptionInput.getText().toString().trim();
        String category = resourceCategoryInput.getText().toString().trim();
        String fileUrl = resourceUrlInput.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description) ||
                TextUtils.isEmpty(category) || TextUtils.isEmpty(fileUrl)) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String resourceId = mDatabase.child("resources").child(sessionId).push().getKey();
        long uploadTime = System.currentTimeMillis();

        Resource resource = new Resource(resourceId, name, description, category, fileUrl,
                uploaderId, uploadTime, sessionId);

        mDatabase.child("resources").child(sessionId).child(resourceId)
                .setValue(resource)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ResourceUploadActivity.this, "Resource uploaded successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ResourceUploadActivity.this, "Failed to upload resource", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
