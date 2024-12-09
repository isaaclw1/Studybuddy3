package com.example.studybuddy3.datatype;

public class ChatMessage {
    private String messageId;
    private String senderId;
    private String receiverId;
    private String senderEmail;
    private String content;
    private long timestamp;
    private String pdfUrl; // New field for PDF messages

    public ChatMessage() {}

    public ChatMessage(String messageId, String senderId, String receiverId,
                       String senderEmail, String content, long timestamp) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderEmail = senderEmail;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Add a constructor that includes pdfUrl
    public ChatMessage(String messageId, String senderId, String receiverId,
                       String senderEmail, String content, long timestamp, String pdfUrl) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderEmail = senderEmail;
        this.content = content;
        this.timestamp = timestamp;
        this.pdfUrl = pdfUrl;
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
}
