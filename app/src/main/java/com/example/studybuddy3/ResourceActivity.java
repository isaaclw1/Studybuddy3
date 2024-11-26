package com.example.studybuddy3;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy3.datatype.Resource;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResourceActivity extends AppCompatActivity {
    private TextInputEditText searchInput;
    private MaterialButton uploadButton;
    private RecyclerView resourcesRecyclerView;
    private TextView noResourcesText;

    private DatabaseReference mDatabase;
    private String sessionId;
    private String groupId;
    private ResourceAdapter resourceAdapter;
    private List<Resource> allResources = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource);

        sessionId = getIntent().getStringExtra("sessionId");
        groupId = getIntent().getStringExtra("groupId");

        if (sessionId == null || groupId == null) {
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();

        initializeViews();
        setupRecyclerView();
        setupSearchInput();
        setupUploadButton();
        loadResources();
    }

    private void initializeViews() {
        searchInput = findViewById(R.id.searchInput);
        uploadButton = findViewById(R.id.uploadButton);
        resourcesRecyclerView = findViewById(R.id.resourcesRecyclerView);
        noResourcesText = findViewById(R.id.noResourcesText);
    }

    private void setupRecyclerView() {
        resourceAdapter = new ResourceAdapter();
        resourcesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resourcesRecyclerView.setAdapter(resourceAdapter);
    }

    private void setupSearchInput() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterResources(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupUploadButton() {
        uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ResourceUploadActivity.class);
            intent.putExtra("sessionId", sessionId);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        });
    }

    private void loadResources() {
        mDatabase.child("resources").child(sessionId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allResources.clear();
                        for (DataSnapshot resourceSnapshot : snapshot.getChildren()) {
                            Resource resource = resourceSnapshot.getValue(Resource.class);
                            if (resource != null) {
                                allResources.add(resource);
                            }
                        }
                        filterResources(searchInput.getText().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ResourceActivity.this,
                                "Failed to load resources: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void filterResources(String query) {
        List<Resource> filteredList = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();

        for (Resource resource : allResources) {
            if (resource.getName().toLowerCase().contains(lowerQuery) ||
                    resource.getDescription().toLowerCase().contains(lowerQuery) ||
                    resource.getCategory().toLowerCase().contains(lowerQuery)) {
                filteredList.add(resource);
            }
        }

        if (filteredList.isEmpty()) {
            noResourcesText.setVisibility(View.VISIBLE);
            resourcesRecyclerView.setVisibility(View.GONE);
        } else {
            noResourcesText.setVisibility(View.GONE);
            resourcesRecyclerView.setVisibility(View.VISIBLE);
            resourceAdapter.setResources(filteredList);
        }
    }


    private class ResourceAdapter extends RecyclerView.Adapter<ResourceAdapter.ResourceViewHolder> {
        private List<Resource> resources = new ArrayList<>();

        public void setResources(List<Resource> resources) {
            this.resources = new ArrayList<>(resources);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ResourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_resource, parent, false);
            return new ResourceViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ResourceViewHolder holder, int position) {
            Resource resource = resources.get(position);
            holder.bind(resource);

            holder.viewButton.setOnClickListener(v -> {
                Intent intent = new Intent(ResourceActivity.this, ResourceViewActivity.class);
                intent.putExtra("resourceId", resource.getResourceId());
                intent.putExtra("sessionId", sessionId);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return resources.size();
        }

        class ResourceViewHolder extends RecyclerView.ViewHolder {
            TextView nameText, descriptionText, categoryText;
            MaterialButton viewButton;

            ResourceViewHolder(@NonNull View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.resourceNameText);
                descriptionText = itemView.findViewById(R.id.resourceDescriptionText);
                categoryText = itemView.findViewById(R.id.resourceCategoryText);
                viewButton = itemView.findViewById(R.id.viewButton);
            }

            void bind(Resource resource) {
                nameText.setText(resource.getName());
                descriptionText.setText(resource.getDescription());
                categoryText.setText(resource.getCategory());
            }
        }
    }
}