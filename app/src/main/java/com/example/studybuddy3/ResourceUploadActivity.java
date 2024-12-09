package com.example.studybuddy3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studybuddy3.datatype.Resource;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class ResourceUploadActivity extends AppCompatActivity {

    private static final int PDF_FILE_PICKER = 1001;

    private EditText resourceNameInput;
    private EditText resourceDescriptionInput;
    private EditText resourceCategoryInput;
    private TextView chosenFileText;
    private Button chooseFileButton;
    private Button uploadResourceButton;

    private Uri selectedPdfUri = null;  // To hold the selected file URI
    private DatabaseReference mDatabase;
    private String sessionId;
    private String uploaderId;

    @SuppressLint("MissingInflatedId")
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
        chosenFileText = findViewById(R.id.chosenFileText);
        chooseFileButton = findViewById(R.id.chooseFileButton);
        uploadResourceButton = findViewById(R.id.uploadResourceButton);

        chooseFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open file picker for PDF
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "Select PDF"), PDF_FILE_PICKER);
            }
        });

        uploadResourceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadResource();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PDF_FILE_PICKER && resultCode == RESULT_OK && data != null) {
            selectedPdfUri = data.getData();
            if (selectedPdfUri != null) {
                chosenFileText.setText(selectedPdfUri.getLastPathSegment());
            } else {
                chosenFileText.setText("No file chosen");
            }
        }
    }

    private void uploadResource() {
        String name = resourceNameInput.getText().toString().trim();
        String description = resourceDescriptionInput.getText().toString().trim();
        String category = resourceCategoryInput.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description) ||
                TextUtils.isEmpty(category) || selectedPdfUri == null) {
            Toast.makeText(this, "Please fill out all fields and choose a PDF file", Toast.LENGTH_SHORT).show();
            return;
        }

        String resourceId = mDatabase.child("resources").child(sessionId).push().getKey();
        long uploadTime = System.currentTimeMillis();

        // Upload the file to Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("resources").child(sessionId).child(resourceId + ".pdf");
        storageRef.putFile(selectedPdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Once the upload is complete, get the download URL
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String fileUrl = uri.toString();

                                Resource resource = new Resource(resourceId, name, description, category, fileUrl,
                                        uploaderId, uploadTime, sessionId);

                                mDatabase.child("resources").child(sessionId).child(resourceId)
                                        .setValue(resource)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(ResourceUploadActivity.this, "Resource uploaded successfully", Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else {
                                                Toast.makeText(ResourceUploadActivity.this, "Failed to upload resource info", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Toast.makeText(ResourceUploadActivity.this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ResourceUploadActivity.this, "File upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}