package com.example.studybuddy3;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.studybuddy3.datatype.Resource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ResourceViewActivity extends AppCompatActivity {

    private WebView resourceWebView;
    private TextView resourceNameText;
    private TextView resourceDescriptionText;
    private TextView resourceCategoryText;
    private TextView uploadDateText;
    private TextView uploaderIdText;
    private DatabaseReference mDatabase;
    private String sessionId;
    private String resourceId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_view);

        // Initialize UI components
        initializeViews();

        // Set up the toolbar
        setupToolbar();

        // Get intent extras
        sessionId = getIntent().getStringExtra("sessionId");
        resourceId = getIntent().getStringExtra("resourceId");

        if (sessionId == null || resourceId == null) {
            Toast.makeText(this, "Invalid session or resource ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        loadResourceDetails();
    }

    private void initializeViews() {
        resourceWebView = findViewById(R.id.resourceWebView);
        resourceNameText = findViewById(R.id.resourceNameText);
        resourceDescriptionText = findViewById(R.id.resourceDescriptionText);
        resourceCategoryText = findViewById(R.id.resourceCategoryText);
        uploaderIdText = findViewById(R.id.uploaderIdText);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Resource Viewer");
        }
    }

    private void loadResourceDetails() {
        mDatabase.child("resources").child(sessionId).child(resourceId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Resource resource = snapshot.getValue(Resource.class);
                        if (resource != null) {
                            displayResourceDetails(resource);
                            setupWebView(resource.getFileUrl());
                        } else {
                            Toast.makeText(ResourceViewActivity.this, "Resource not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ResourceViewActivity.this,
                                "Failed to load resource: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void displayResourceDetails(Resource resource) {
        resourceNameText.setText(resource.getName());
        resourceDescriptionText.setText(resource.getDescription());
        resourceCategoryText.setText(resource.getCategory());

        // Format the upload date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

        uploaderIdText.setText("Uploaded by: " + resource.getUploaderId());
    }

    private void setupWebView(String url) {
        WebSettings webSettings = resourceWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        resourceWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Hide loading indicator if you have one
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(ResourceViewActivity.this,
                        "Error loading PDF: " + description,
                        Toast.LENGTH_SHORT).show();
            }
        });

        resourceWebView.loadUrl("https://docs.google.com/gview?embedded=true&url=" + url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (resourceWebView != null) {
            resourceWebView.destroy();
        }
        super.onDestroy();
    }
}