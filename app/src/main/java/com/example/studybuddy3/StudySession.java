package com.example.studybuddy3;

import java.util.ArrayList;
import java.util.List;

public class StudySession {
    private String sessionId;
    private String title;
    private int dateTime;  // Store as hours (0 to 23)
    private int endTime;   // Must be after dateTime and within the same day (0 to 23)
    private String location;
    private List<String> attendeeIds;

    public StudySession() {
        // Required empty constructor for Firebase
        this.attendeeIds = new ArrayList<>();
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

    public int getDateTime() {
        return dateTime;
    }

    public void setDateTime(int dateTime) {
        if (dateTime < 0 || dateTime > 23) {
            throw new IllegalArgumentException("dateTime must be between 0 and 23.");
        }
        this.dateTime = dateTime;

        // Reset endTime to ensure it's not before the new start time
        if (this.endTime <= dateTime) {
            this.endTime = dateTime + 1;  // Default to 1 hour after dateTime if endTime is invalid
        }
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        if (endTime <= dateTime || endTime > 23) {
            throw new IllegalArgumentException("endTime must be greater than dateTime and within the range 0 to 23.");
        }
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
}
