package com.example.studybuddy3.datatype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Course {
    private String courseId;
    private String courseName;
    private String courseCode;
    private String description;
    private List<String> enrolledUserIds;
    private Map<String, String> groupIds;

    // Default constructor initializes the lists and maps
    public Course() {
        this.enrolledUserIds = new ArrayList<>();
        this.groupIds = new HashMap<>();
    }

    // Non-default constructor
    public Course(String courseId, String courseName, String courseCode, String description) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.description = description;
        this.enrolledUserIds = new ArrayList<>();
        this.groupIds = new HashMap<>();
    }

    // Getter and Setter for courseId
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    // Getter and Setter for courseName
    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    // Getter and Setter for courseCode
    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    // Getter and Setter for description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Getter and Setter for enrolledUserIds
    public List<String> getEnrolledUserIds() {
        return enrolledUserIds != null ? enrolledUserIds : new ArrayList<>();
    }

    public void setEnrolledUserIds(List<String> enrolledUserIds) {
        this.enrolledUserIds = enrolledUserIds;
    }

    // Getter and Setter for groupIds
    public Map<String, String> getGroupIds() {
        return groupIds != null ? groupIds : new HashMap<>();
    }

    public void setGroupIds(Map<String, String> groupIds) {
        this.groupIds = groupIds;
    }
}
