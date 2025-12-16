package com.example.test_spring.dto;

import com.example.test_spring.model.Complaint;
import com.example.test_spring.model.Notification;

import java.util.List;

public class DashboardResponse {
    private String firstName;
    private String email;
    private long totalComplaints;
    private long pendingComplaints;
    private long inProgressComplaints;
    private long resolvedComplaints;
    private List<Complaint> recentComplaints;
    private List<Notification> notifications;
    private long unreadNotifications;
    
    // Default constructor
    public DashboardResponse() {}
    
    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public long getTotalComplaints() {
        return totalComplaints;
    }
    
    public void setTotalComplaints(long totalComplaints) {
        this.totalComplaints = totalComplaints;
    }
    
    public long getPendingComplaints() {
        return pendingComplaints;
    }
    
    public void setPendingComplaints(long pendingComplaints) {
        this.pendingComplaints = pendingComplaints;
    }
    
    public long getInProgressComplaints() {
        return inProgressComplaints;
    }
    
    public void setInProgressComplaints(long inProgressComplaints) {
        this.inProgressComplaints = inProgressComplaints;
    }
    
    public long getResolvedComplaints() {
        return resolvedComplaints;
    }
    
    public void setResolvedComplaints(long resolvedComplaints) {
        this.resolvedComplaints = resolvedComplaints;
    }
    
    public List<Complaint> getRecentComplaints() {
        return recentComplaints;
    }
    
    public void setRecentComplaints(List<Complaint> recentComplaints) {
        this.recentComplaints = recentComplaints;
    }
    
    public List<Notification> getNotifications() {
        return notifications;
    }
    
    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }
    
    public long getUnreadNotifications() {
        return unreadNotifications;
    }
    
    public void setUnreadNotifications(long unreadNotifications) {
        this.unreadNotifications = unreadNotifications;
    }
}