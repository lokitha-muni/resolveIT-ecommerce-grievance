package com.example.test_spring.controller;

import com.example.test_spring.dto.DashboardResponse;
import com.example.test_spring.model.Complaint;
import com.example.test_spring.service.DashboardService;
import com.example.test_spring.repository.ComplaintRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {
    
    @Autowired
    private DashboardService dashboardService;
    
    @Autowired
    private ComplaintRepository complaintRepository;
    
    @GetMapping("/{email}")
    public ResponseEntity<DashboardResponse> getDashboard(@PathVariable String email) {
        try {
            DashboardResponse dashboardData = dashboardService.getDashboardData(email);
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/init/{email}")
    public ResponseEntity<String> initializeSampleData(@PathVariable String email) {
        try {
            dashboardService.initializeSampleData(email);
            return ResponseEntity.ok("Sample data initialized");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to initialize data");
        }
    }
    
    @GetMapping("/complaint/{complaintId}")
    public ResponseEntity<Complaint> getComplaintDetails(@PathVariable String complaintId) {
        try {
            Complaint complaint = dashboardService.getComplaintById(complaintId);
            if (complaint != null) {
                return ResponseEntity.ok(complaint);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/search/{email}")
    public ResponseEntity<List<Complaint>> searchComplaints(
            @PathVariable String email,
            @RequestParam(required = false) String complaintId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        
        try {
            List<Complaint> complaints = complaintRepository.findByUserEmailOrderByUpdatedAtDesc(email);
            
            // Filter by complaint ID
            if (complaintId != null && !complaintId.isEmpty()) {
                complaints = complaints.stream()
                    .filter(c -> c.getComplaintId().toLowerCase().contains(complaintId.toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            // Filter by status
            if (status != null && !status.isEmpty()) {
                complaints = complaints.stream()
                    .filter(c -> c.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
            }
            
            // Filter by date range
            if (fromDate != null && !fromDate.isEmpty()) {
                LocalDateTime from = LocalDateTime.parse(fromDate + "T00:00:00");
                complaints = complaints.stream()
                    .filter(c -> c.getCreatedAt().isAfter(from))
                    .collect(Collectors.toList());
            }
            
            if (toDate != null && !toDate.isEmpty()) {
                LocalDateTime to = LocalDateTime.parse(toDate + "T23:59:59");
                complaints = complaints.stream()
                    .filter(c -> c.getCreatedAt().isBefore(to))
                    .collect(Collectors.toList());
            }
            
            return ResponseEntity.ok(complaints);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/export/{email}")
    public ResponseEntity<String> exportComplaints(@PathVariable String email, @RequestParam String format) {
        try {
            List<Complaint> complaints = complaintRepository.findByUserEmailOrderByUpdatedAtDesc(email);
            
            if ("csv".equalsIgnoreCase(format)) {
                String csv = generateCSV(complaints);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.TEXT_PLAIN);
                headers.setContentDispositionFormData("attachment", "complaints.csv");
                return ResponseEntity.ok().headers(headers).body(csv);
            } else {
                return ResponseEntity.badRequest().body("Unsupported format");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    private String generateCSV(List<Complaint> complaints) {
        StringBuilder csv = new StringBuilder();
        csv.append("Complaint ID,Order ID,Issue Type,Status,Description,Created Date,Updated Date\n");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (Complaint complaint : complaints) {
            csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                complaint.getComplaintId(),
                complaint.getOrderId(),
                complaint.getIssueType(),
                complaint.getStatus(),
                complaint.getDescription().replace("\"", "\"\""),
                complaint.getCreatedAt().format(formatter),
                complaint.getUpdatedAt().format(formatter)
            ));
        }
        
        return csv.toString();
    }
}