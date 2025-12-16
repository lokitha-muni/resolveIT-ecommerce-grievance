package com.example.test_spring.dto;

public class ComplaintRequest {
    private String orderId;
    private String orderDate;
    private String issueType;
    private String issueTitle;
    private String issueDescription;
    private String priority;
    private String contactPhone;
    private String expectedResolution;
    
    // Default constructor
    public ComplaintRequest() {}
    
    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }
    
    public String getIssueType() {
        return issueType;
    }
    
    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }
    
    public String getIssueTitle() {
        return issueTitle;
    }
    
    public void setIssueTitle(String issueTitle) {
        this.issueTitle = issueTitle;
    }
    
    public String getIssueDescription() {
        return issueDescription;
    }
    
    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getContactPhone() {
        return contactPhone;
    }
    
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
    
    public String getExpectedResolution() {
        return expectedResolution;
    }
    
    public void setExpectedResolution(String expectedResolution) {
        this.expectedResolution = expectedResolution;
    }
}