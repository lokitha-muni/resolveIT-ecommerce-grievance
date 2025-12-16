package com.example.test_spring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "staff_notes")
public class StaffNote {
    
    @Id
    private String id;
    
    private String complaintId;
    private String staffEmail;
    private String note;
    private boolean isInternal; // true for internal notes, false for customer visible
    private LocalDateTime createdAt;
    
    public StaffNote() {
        this.createdAt = LocalDateTime.now();
        this.isInternal = true;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getComplaintId() { return complaintId; }
    public void setComplaintId(String complaintId) { this.complaintId = complaintId; }
    
    public String getStaffEmail() { return staffEmail; }
    public void setStaffEmail(String staffEmail) { this.staffEmail = staffEmail; }
    
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    
    public boolean isInternal() { return isInternal; }
    public void setInternal(boolean internal) { isInternal = internal; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}