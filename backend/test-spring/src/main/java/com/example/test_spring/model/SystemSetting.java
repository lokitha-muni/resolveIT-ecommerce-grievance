package com.example.test_spring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "system_settings")
public class SystemSetting {
    
    @Id
    private String id;
    
    private String key;
    private String value;
    private String description;
    private String category; // COMPLAINT_CATEGORIES, PRIORITIES, GENERAL
    private LocalDateTime updatedAt;
    
    public SystemSetting() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public SystemSetting(String key, String value, String description, String category) {
        this();
        this.key = key;
        this.value = value;
        this.description = description;
        this.category = category;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}