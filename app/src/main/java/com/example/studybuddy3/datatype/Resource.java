package com.example.studybuddy3.datatype;

public class Resource {
    private String resourceId;
    private String name;
    private String description;
    private String category;    // e.g., "Lecture Notes", "Practice Exams", "Project Help"
    private String fileUrl;     // URL to the stored file
    private String uploaderId;  // ID of user who uploaded
    private String sessionId;   // Reference to parent study session

    // Required empty constructor for Firebase
    public Resource() {
    }

    // Full constructor
    public Resource(String resourceId, String name, String description, String category,
                    String fileUrl, String uploaderId, long uploadTime, String sessionId) {
        this.resourceId = resourceId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.fileUrl = fileUrl;
        this.uploaderId = uploaderId;
        this.sessionId = sessionId;
    }

    // Getters and Setters
    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(String uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
