package com.example.test_spring.controller;

import com.example.test_spring.dto.ComplaintRequest;
import com.example.test_spring.model.Complaint;
import com.example.test_spring.model.Notification;
import com.example.test_spring.repository.ComplaintRepository;
import com.example.test_spring.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = "*")
public class ComplaintController {
    
    @Autowired
    private ComplaintRepository complaintRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    private final String UPLOAD_DIR = "uploads/";
    
    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submitComplaint(
            @RequestBody ComplaintRequest request,
            @RequestParam String userEmail) {
        
        System.out.println("Complaint submission received for user: " + userEmail);
        System.out.println("Request data: " + request.getIssueType() + " - " + request.getIssueTitle());
        
        Map<String, String> response = new HashMap<>();
        
        try {
            // Generate complaint ID
            String complaintId = "CMP-" + System.currentTimeMillis();
            
            // Create complaint
            Complaint complaint = new Complaint();
            complaint.setComplaintId(complaintId);
            complaint.setUserEmail(userEmail);
            complaint.setOrderId(request.getOrderId());
            complaint.setIssueType(request.getIssueType());
            complaint.setDescription(request.getIssueDescription());
            complaint.setStatus("PENDING");
            complaint.setPriority(request.getPriority());
            complaint.setContactPhone(request.getContactPhone());
            complaint.setExpectedResolution(request.getExpectedResolution());
            
            // Save complaint
            complaintRepository.save(complaint);
            
            // Create notification
            Notification notification = new Notification(
                userEmail,
                "Complaint Submitted",
                "Your complaint " + complaintId + " has been submitted successfully",
                "SUCCESS"
            );
            notificationRepository.save(notification);
            
            response.put("status", "success");
            response.put("message", "Complaint submitted successfully");
            response.put("complaintId", complaintId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error submitting complaint: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Failed to submit complaint: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/submit-with-files")
    public ResponseEntity<Map<String, String>> submitComplaintWithFiles(
            @RequestParam String userEmail,
            @RequestParam String orderId,
            @RequestParam String orderDate,
            @RequestParam String issueType,
            @RequestParam String issueTitle,
            @RequestParam String issueDescription,
            @RequestParam String priority,
            @RequestParam(required = false) String contactPhone,
            @RequestParam(required = false) String expectedResolution,
            @RequestParam(required = false) MultipartFile[] attachments) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            // Generate complaint ID
            String complaintId = "CMP-" + System.currentTimeMillis();
            
            // Handle file uploads
            List<String> filePaths = new ArrayList<>();
            if (attachments != null && attachments.length > 0) {
                filePaths = saveUploadedFiles(attachments, complaintId);
            }
            
            // Create complaint
            Complaint complaint = new Complaint();
            complaint.setComplaintId(complaintId);
            complaint.setUserEmail(userEmail);
            complaint.setOrderId(orderId);
            complaint.setIssueType(issueType);
            complaint.setDescription(issueDescription);
            complaint.setStatus("PENDING");
            complaint.setPriority(priority);
            complaint.setContactPhone(contactPhone);
            complaint.setExpectedResolution(expectedResolution);
            complaint.setAttachments(filePaths);
            
            // Save complaint
            complaintRepository.save(complaint);
            
            // Create notification
            Notification notification = new Notification(
                userEmail,
                "Complaint Submitted",
                "Your complaint " + complaintId + " has been submitted successfully",
                "SUCCESS"
            );
            notificationRepository.save(notification);
            
            response.put("status", "success");
            response.put("message", "Complaint submitted successfully");
            response.put("complaintId", complaintId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error submitting complaint with files: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Failed to submit complaint: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    private List<String> saveUploadedFiles(MultipartFile[] files, String complaintId) throws IOException {
        List<String> filePaths = new ArrayList<>();
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR + complaintId);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath);
                filePaths.add(filePath.toString());
            }
        }
        
        return filePaths;
    }

    @GetMapping("/user/{userEmail}")
    public ResponseEntity<List<Complaint>> getUserComplaints(@PathVariable String userEmail) {
        try {
            List<Complaint> complaints = complaintRepository.findByUserEmailOrderByUpdatedAtDesc(userEmail);
            System.out.println("Found " + complaints.size() + " complaints for user: " + userEmail);
            return ResponseEntity.ok(complaints);
        } catch (Exception e) {
            System.err.println("Error fetching user complaints: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/init-sample-data")
    public ResponseEntity<Map<String, String>> initSampleData() {
        try {
            return ResponseEntity.ok(Map.of("status", "success", "message", "Sample data endpoint is working"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @PostMapping("/create-sample")
    public ResponseEntity<Map<String, String>> createSample() {
        try {
            for (int i = 1; i <= 3; i++) {
                Complaint complaint = new Complaint();
                complaint.setComplaintId("CMP-" + String.format("%04d", i));
                complaint.setUserEmail("test@gmail.com");
                complaint.setOrderId("ORD-10000" + i);
                complaint.setIssueType(i == 1 ? "Late Delivery" : i == 2 ? "Wrong Product" : "Damaged Product");
                complaint.setDescription("Sample complaint #" + i);
                complaint.setStatus(i == 1 ? "PENDING" : i == 2 ? "IN_PROGRESS" : "RESOLVED");
                complaint.setPriority("Medium");
                complaintRepository.save(complaint);
            }
            return ResponseEntity.ok(Map.of("status", "success", "message", "Created 3 sample complaints"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @GetMapping("/{complaintId}")
    public ResponseEntity<Complaint> getComplaint(@PathVariable String complaintId) {
        try {
            Optional<Complaint> complaint = complaintRepository.findByComplaintId(complaintId);
            if (complaint.isPresent()) {
                return ResponseEntity.ok(complaint.get());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Error fetching complaint: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}