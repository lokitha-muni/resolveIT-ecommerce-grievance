package com.example.test_spring.service;

import com.example.test_spring.dto.DashboardResponse;
import com.example.test_spring.model.Complaint;
import com.example.test_spring.model.Notification;
import com.example.test_spring.model.User;
import com.example.test_spring.repository.ComplaintRepository;
import com.example.test_spring.repository.NotificationRepository;
import com.example.test_spring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DashboardService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ComplaintRepository complaintRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    public DashboardResponse getDashboardData(String email) {
        DashboardResponse response = new DashboardResponse();
        
        // Get user info
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            response.setEmail(email);
            
            // Extract first name from full name
            String fullName = user.getFullName();
            String firstName = fullName != null ? fullName.split(" ")[0] : "User";
            response.setFirstName(firstName);
        }
        
        // Get complaint statistics
        response.setTotalComplaints(complaintRepository.countByUserEmail(email));
        response.setPendingComplaints(complaintRepository.countByUserEmailAndStatus(email, "PENDING"));
        response.setInProgressComplaints(complaintRepository.countByUserEmailAndStatus(email, "IN_PROGRESS"));
        response.setResolvedComplaints(complaintRepository.countByUserEmailAndStatus(email, "RESOLVED"));
        
        // Get recent complaints (last 5)
        List<Complaint> allComplaints = complaintRepository.findByUserEmailOrderByUpdatedAtDesc(email);
        List<Complaint> recentComplaints = allComplaints.size() > 5 ? 
            allComplaints.subList(0, 5) : allComplaints;
        response.setRecentComplaints(recentComplaints);
        
        // Get notifications
        List<Notification> notifications = notificationRepository.findByUserEmailOrderByCreatedAtDesc(email);
        long unreadCount = notificationRepository.countByUserEmailAndIsRead(email, false);
        response.setNotifications(notifications);
        response.setUnreadNotifications(unreadCount);
        
        return response;
    }
    
    public void initializeSampleData(String email) {
        // Create sample complaints for demo
        if (complaintRepository.countByUserEmail(email) == 0) {
            createSampleComplaint(email, "CMP-001", "ORD-12345", "Wrong Product", "Received wrong item in my order");
            createSampleComplaint(email, "CMP-002", "ORD-67890", "Late Delivery", "Order delivered 3 days late");
            createSampleComplaint(email, "CMP-003", "ORD-11223", "Damaged Product", "Product arrived damaged");
        }
        
        // Create sample notifications
        if (notificationRepository.countByUserEmailAndIsRead(email, false) == 0) {
            createSampleNotification(email, "Complaint Update", "Your complaint CMP-001 has been resolved", "SUCCESS");
            createSampleNotification(email, "New Message", "Support team has responded to your complaint CMP-002", "INFO");
            createSampleNotification(email, "Welcome", "Welcome to ResolveIT! Your account is now active", "INFO");
        }
    }
    
    private void createSampleNotification(String email, String title, String message, String type) {
        Notification notification = new Notification(email, title, message, type);
        notificationRepository.save(notification);
    }
    
    private void createSampleComplaint(String email, String complaintId, String orderId, String issueType, String description) {
        Complaint complaint = new Complaint();
        complaint.setComplaintId(complaintId);
        complaint.setUserEmail(email);
        complaint.setOrderId(orderId);
        complaint.setIssueType(issueType);
        complaint.setDescription(description);
        
        // Set random status for demo
        if (complaintId.equals("CMP-001")) {
            complaint.setStatus("RESOLVED");
        } else if (complaintId.equals("CMP-002")) {
            complaint.setStatus("IN_PROGRESS");
        } else {
            complaint.setStatus("PENDING");
        }
        
        complaintRepository.save(complaint);
        
        // Create notification for new complaint
        String notificationTitle = "Complaint Submitted";
        String notificationMessage = "Your complaint " + complaintId + " has been submitted successfully";
        createSampleNotification(email, notificationTitle, notificationMessage, "SUCCESS");
    }
    
    public Complaint getComplaintById(String complaintId) {
        List<Complaint> complaints = complaintRepository.findAll();
        return complaints.stream()
            .filter(c -> c.getComplaintId().equals(complaintId))
            .findFirst()
            .orElse(null);
    }
}