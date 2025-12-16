package com.example.test_spring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notifications")
public class Notification {
    
    @Id
    private String id;
    
    private String userEmail;
    private String title;
    private String message;
    private String type; // INFO, SUCCESS, WARNING, ERROR
    private boolean isRead;
    private LocalDateTime createdAt;
    
    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }
    
    public Notification(String userEmail, String title, String message, String type) {
        this();
        this.userEmail = userEmail;
        this.title = title;
        this.message = message;
        this.type = type;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}