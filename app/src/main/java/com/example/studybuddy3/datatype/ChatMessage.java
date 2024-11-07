package com.example.studybuddy3.datatype;

public class ChatMessage {
    private String messageId;
    private String senderId;
    private String receiverId;  // Add this field
    private String senderEmail;
    private String content;
    private long timestamp;

    // Required empty constructor for Firebase
    public ChatMessage() {}

    // Updated constructor
    public ChatMessage(String messageId, String senderId, String receiverId,
                       String senderEmail, String content, long timestamp) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderEmail = senderEmail;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getter and Setter for messageId
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    // Getter and Setter for senderId
    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    // Getter and Setter for receiverId
    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    // Getter and Setter for senderEmail
    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    // Getter and Setter for content
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // Getter and Setter for timestamp
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
