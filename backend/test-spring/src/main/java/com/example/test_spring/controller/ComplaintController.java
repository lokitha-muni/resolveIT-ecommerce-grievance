package com.example.test_spring.controller;

import com.example.test_spring.dto.ComplaintRequest;
import com.example.test_spring.model.Complaint;
import com.example.test_spring.model.Notification;
import com.example.test_spring.repository.ComplaintRepository;
import com.example.test_spring.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        
        Map<String, String> response = new HashMap<>();
        
        try {
            String complaintId = "CMP-" + System.currentTimeMillis();
            
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
            
            complaintRepository.save(complaint);
            
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
            String complaintId = "CMP-" + System.currentTimeMillis();
            
            List<String> filePaths = new ArrayList<>();
            if (attachments != null && attachments.length > 0) {
                filePaths = saveUploadedFiles(attachments, complaintId);
            }
            
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
            
            complaintRepository.save(complaint);
            
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
            response.put("status", "error");
            response.put("message", "Failed to submit complaint: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    private List<String> saveUploadedFiles(MultipartFile[] files, String complaintId) throws IOException {
        List<String> filePaths = new ArrayList<>();
        
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
            return ResponseEntity.ok(complaints);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Complaint>> getAllComplaints() {
        try {
            List<Complaint> complaints = complaintRepository.findAll();
            return ResponseEntity.ok(complaints);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/file/{complaintId}/{fileName}")
    public ResponseEntity<org.springframework.core.io.Resource> getFile(
            @PathVariable String complaintId, 
            @PathVariable String fileName) {
        try {
            String decodedFileName = java.net.URLDecoder.decode(fileName, "UTF-8");
            Path filePath = Paths.get(UPLOAD_DIR + complaintId + "/" + decodedFileName);
            
            if (!Files.exists(filePath)) {
                Path uploadDir = Paths.get(UPLOAD_DIR + complaintId);
                if (Files.exists(uploadDir)) {
                    Optional<Path> matchingFile = Files.list(uploadDir)
                        .filter(f -> f.getFileName().toString().contains(decodedFileName.substring(0, Math.min(20, decodedFileName.length()))))
                        .findFirst();
                    
                    if (matchingFile.isPresent()) {
                        filePath = matchingFile.get();
                    }
                }
            }
            
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                String contentType = "application/octet-stream";
                String fileNameLower = filePath.getFileName().toString().toLowerCase();
                if (fileNameLower.endsWith(".jpg") || fileNameLower.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (fileNameLower.endsWith(".png")) {
                    contentType = "image/png";
                } else if (fileNameLower.endsWith(".gif")) {
                    contentType = "image/gif";
                }
                
                return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, contentType)
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filePath.getFileName() + "\"")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
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
            return ResponseEntity.internalServerError().build();
        }
    }
}