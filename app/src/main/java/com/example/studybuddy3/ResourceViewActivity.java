package com.example.studybuddy3;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studybuddy3.datatype.Resource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ResourceViewActivity extends AppCompatActivity {

    private WebView resourceWebView;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_view);

        resourceWebView = findViewById(R.id.resourceWebView);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Retrieve sessionId and resourceId from intent extras
        String sessionId = getIntent().getStringExtra("sessionId");
        String resourceId = getIntent().getStringExtra("resourceId");

        if (sessionId == null || resourceId == null) {
            Toast.makeText(this, "Invalid session or resource ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load the resource URL directly from the resources path
        loadResourceUrl(sessionId, resourceId);
    }

    private void loadResourceUrl(String sessionId, String resourceId) {
        mDatabase.child("resources").child(sessionId).child(resourceId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Resource resource = snapshot.getValue(Resource.class);
                        if (resource != null && resource.getFileUrl() != null) {
                            setupWebView(resource.getFileUrl());
                        } else {
                            Toast.makeText(ResourceViewActivity.this, "Resource URL not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ResourceViewActivity.this, "Failed to load resource: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void setupWebView(String url) {
        // Configure WebView settings
        WebSettings webSettings = resourceWebView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Enable JavaScript if needed

        // Load the URL in the WebView
        resourceWebView.setWebViewClient(new WebViewClient()); // Ensures the URL opens in the app
        resourceWebView.loadUrl(url);
    }
}
