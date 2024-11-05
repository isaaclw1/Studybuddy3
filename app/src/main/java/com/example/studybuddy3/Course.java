package com.example.studybuddy3;

import java.util.ArrayList;
import java.util.List;

public class Course {
    private String courseId;
    private String courseName;
    private String courseCode;
    private String description;
    private List<String> enrolledUserIds;  // List of enrolled user IDs
    private List<String> groupIds;         // List of study group IDs

    public Course() {
        this.enrolledUserIds = new ArrayList<>();
        this.groupIds = new ArrayList<>();
    }

    public Course(String courseId, String courseName, String courseCode, String description) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.description = description;
        this.enrolledUserIds = new ArrayList<>();
        this.groupIds = new ArrayList<>();
    }

    // Getters and Setters
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getEnrolledUserIds() {
        return enrolledUserIds;
    }

    public void setEnrolledUserIds(List<String> enrolledUserIds) {
        this.enrolledUserIds = enrolledUserIds;
    }

    public List<String> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(List<String> groupIds) {
        this.groupIds = groupIds;
    }
}
