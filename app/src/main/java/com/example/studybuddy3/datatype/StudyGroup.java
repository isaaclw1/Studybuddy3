package com.example.studybuddy3.datatype;

import java.util.ArrayList;
import java.util.List;

public class StudyGroup {
    private String groupId;
    private String courseId;
    private String groupName;
    private List<String> memberIds;  // List of user IDs
    private List<StudySession> sessions;
    private List<ChatMessage> messages;
    private long createdAt;

    public StudyGroup() {
        // Required empty constructor for Firebase
        this.memberIds = new ArrayList<>();
        this.sessions = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    // Getters and Setters
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    public List<StudySession> getSessions() {
        return sessions;
    }

    public void setSessions(List<StudySession> sessions) {
        this.sessions = sessions;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}

