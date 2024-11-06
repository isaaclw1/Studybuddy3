package com.example.studybuddy3.datatype;

// StudySession.java
import java.util.ArrayList;
import java.util.List;

public class StudySession {
    private String sessionId;
    private String title;
    private long date;         // stored as milliseconds
    private long startTime;    // stored as milliseconds
    private long endTime;      // stored as milliseconds
    private String location;
    private List<String> attendeeIds;
    private List<Resource> resources;
    private String groupId;    // reference to parent group

    // Required empty constructor for Firebase
    public StudySession() {
        this.attendeeIds = new ArrayList<>();
        this.resources = new ArrayList<>();
    }

    // Full constructor
    public StudySession(String sessionId, String title, long date, long startTime,
                        long endTime, String location, String groupId) {
        this.sessionId = sessionId;
        this.title = title;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.groupId = groupId;
        this.attendeeIds = new ArrayList<>();
        this.resources = new ArrayList<>();
    }

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getAttendeeIds() {
        return attendeeIds;
    }

    public void setAttendeeIds(List<String> attendeeIds) {
        this.attendeeIds = attendeeIds;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
