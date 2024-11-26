package com.example.studybuddy3.datatype;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String userId;
    private String email;
    private String passwordHash;
    private List<String> enrolledCourses;

    public User() {
        // Required empty constructor for Firebase
    }

    public User(String userId, String email, String passwordHash) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.enrolledCourses = new ArrayList<>();
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public List<String> getEnrolledCourses() {
        return enrolledCourses;
    }

    public void setEnrolledCourses(List<String> enrolledCourses) {
        this.enrolledCourses = enrolledCourses;
    }

}
