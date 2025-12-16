package com.example.test_spring.controller;

import com.example.test_spring.model.Complaint;
import com.example.test_spring.model.Staff;
import com.example.test_spring.model.StaffNote;
import com.example.test_spring.repository.ComplaintRepository;
import com.example.test_spring.repository.StaffRepository;
import com.example.test_spring.repository.StaffNoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "*")
public class StaffController {
    
    @Autowired
    private StaffRepository staffRepository;
    
    @Autowired
    private ComplaintRepository complaintRepository;
    
    @Autowired
    private StaffNoteRepository staffNoteRepository;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = loginData.get("email");
            String password = loginData.get("password");
            
            // Hardcoded credentials for testing
            if ("staff@gmail.com".equals(email) && "staff@123".equals(password)) {
                response.put("status", "success");
                response.put("message", "Login successful");
                response.put("staff", Map.of(
                    "email", "staff@gmail.com",
                    "firstName", "Support",
                    "lastName", "Agent",
                    "role", "STAFF",
                    "department", "Customer Support"
                ));
                return ResponseEntity.ok(response);
            }
            
            if ("admin@gmail.com".equals(email) && "admin@123".equals(password)) {
                response.put("status", "success");
                response.put("message", "Login successful");
                response.put("staff", Map.of(
                    "email", "admin@gmail.com",
                    "firstName", "System",
                    "lastName", "Administrator",
                    "role", "ADMIN",
                    "department", "Administration"
                ));
                return ResponseEntity.ok(response);
            }
            
            response.put("status", "error");
            response.put("message", "Invalid credentials");
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Login failed");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/dashboard/{email}")
    public ResponseEntity<Map<String, Object>> getDashboard(@PathVariable String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get staff data from database or use defaults
            Optional<Staff> staffOptional = staffRepository.findByEmail(email);
            if (staffOptional.isPresent()) {
                Staff staff = staffOptional.get();
                response.put("firstName", staff.getFirstName());
                response.put("lastName", staff.getLastName());
                response.put("email", staff.getEmail());
                response.put("role", staff.getRole());
                response.put("department", staff.getDepartment());
            } else {
                // Return default data for hardcoded accounts
                if ("staff@gmail.com".equals(email)) {
                    response.put("firstName", "Support");
                    response.put("lastName", "Agent");
                    response.put("email", email);
                    response.put("role", "STAFF");
                    response.put("department", "Customer Support");
                } else if ("admin@gmail.com".equals(email)) {
                    response.put("firstName", "System");
                    response.put("lastName", "Administrator");
                    response.put("email", email);
                    response.put("role", "ADMIN");
                    response.put("department", "Administration");
                } else {
                    return ResponseEntity.notFound().build();
                }
            }
            
            // Get complaint statistics
            long totalComplaints = complaintRepository.count();
            long pendingComplaints = complaintRepository.countByStatus("PENDING");
            long inProgressComplaints = complaintRepository.countByStatus("IN_PROGRESS");
            long resolvedComplaints = complaintRepository.countByStatus("RESOLVED");
            
            // Get recent complaints assigned to this staff member
            List<Complaint> allComplaints = complaintRepository.findTop10ByOrderByCreatedAtDesc();
            List<Complaint> recentComplaints = allComplaints.stream()
                .filter(c -> email.equals(c.getAssignedTo()))
                .collect(Collectors.toList());
            
            response.put("totalComplaints", totalComplaints);
            response.put("pendingComplaints", pendingComplaints);
            response.put("inProgressComplaints", inProgressComplaints);
            response.put("resolvedComplaints", resolvedComplaints);
            response.put("recentComplaints", recentComplaints);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/queue")
    public ResponseEntity<List<Complaint>> getQueue() {
        try {
            // Show all complaints for queue management (staff can assign/reassign)
            List<Complaint> complaints = complaintRepository.findByOrderByCreatedAtDesc();
            return ResponseEntity.ok(complaints);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/my-queue/{email}")
    public ResponseEntity<List<Complaint>> getMyQueue(@PathVariable String email) {
        try {
            // Show only complaints assigned to this staff member
            List<Complaint> allComplaints = complaintRepository.findByOrderByCreatedAtDesc();
            List<Complaint> myComplaints = allComplaints.stream()
                .filter(c -> email.equals(c.getAssignedTo()))
                .collect(Collectors.toList());
            return ResponseEntity.ok(myComplaints);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/complaint/{complaintId}")
    public ResponseEntity<Complaint> getComplaintDetails(@PathVariable String complaintId) {
        try {
            Optional<Complaint> complaintOptional = complaintRepository.findByComplaintId(complaintId);
            if (complaintOptional.isPresent()) {
                return ResponseEntity.ok(complaintOptional.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/complaint/{complaintId}/status")
    public ResponseEntity<Map<String, String>> updateComplaintStatus(
            @PathVariable String complaintId, 
            @RequestBody Map<String, String> statusData) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            Optional<Complaint> complaintOptional = complaintRepository.findByComplaintId(complaintId);
            if (complaintOptional.isPresent()) {
                Complaint complaint = complaintOptional.get();
                complaint.setStatus(statusData.get("status"));
                complaint.setUpdatedAt(java.time.LocalDateTime.now());
                complaintRepository.save(complaint);
                
                response.put("status", "success");
                response.put("message", "Status updated successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Complaint not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to update status");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    

    
    @GetMapping("/profile/{email}")
    public ResponseEntity<Staff> getProfile(@PathVariable String email) {
        try {
            Optional<Staff> staffOptional = staffRepository.findByEmail(email);
            if (staffOptional.isPresent()) {
                Staff staff = staffOptional.get();
                staff.setPassword(null); // Don't send password
                return ResponseEntity.ok(staff);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/profile/{email}")
    public ResponseEntity<Map<String, String>> updateProfile(
            @PathVariable String email, 
            @RequestBody Map<String, String> updatedData) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            Optional<Staff> staffOptional = staffRepository.findByEmail(email);
            if (staffOptional.isPresent()) {
                Staff staff = staffOptional.get();
                
                // Update profile data
                if (updatedData.containsKey("firstName")) {
                    staff.setFirstName(updatedData.get("firstName"));
                }
                if (updatedData.containsKey("lastName")) {
                    staff.setLastName(updatedData.get("lastName"));
                }
                if (updatedData.containsKey("phone")) {
                    staff.setPhone(updatedData.get("phone"));
                }
                if (updatedData.containsKey("department")) {
                    staff.setDepartment(updatedData.get("department"));
                }
                
                staff.setUpdatedAt(java.time.LocalDateTime.now());
                staffRepository.save(staff);
                
                response.put("status", "success");
                response.put("message", "Profile updated successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Staff not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to update profile");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody Map<String, String> logoutData) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String email = logoutData.get("email");
            // Log the logout action
            System.out.println("Staff logout: " + email);
            
            response.put("status", "success");
            response.put("message", "Logged out successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Logout failed");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PutMapping("/assign")
    public ResponseEntity<Map<String, String>> assignComplaint(@RequestBody Map<String, String> assignmentData) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String complaintId = assignmentData.get("complaintId");
            String staffEmail = assignmentData.get("staffEmail");
            
            List<Complaint> complaints = complaintRepository.findAll();
            Complaint complaint = complaints.stream()
                .filter(c -> c.getComplaintId().equals(complaintId))
                .findFirst()
                .orElse(null);
                
            if (complaint != null) {
                complaint.setAssignedTo(staffEmail);
                complaintRepository.save(complaint);
                
                response.put("status", "success");
                response.put("message", "Complaint assigned successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Complaint not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to assign complaint");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PutMapping("/bulk-update")
    public ResponseEntity<Map<String, String>> bulkUpdateComplaints(@RequestBody Map<String, Object> bulkData) {
        Map<String, String> response = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<String> complaintIds = (List<String>) bulkData.get("complaintIds");
            String newStatus = (String) bulkData.get("status");
            String assignTo = (String) bulkData.get("assignTo");
            
            List<Complaint> complaints = complaintRepository.findAll();
            int updatedCount = 0;
            
            for (String complaintId : complaintIds) {
                Complaint complaint = complaints.stream()
                    .filter(c -> c.getComplaintId().equals(complaintId))
                    .findFirst()
                    .orElse(null);
                    
                if (complaint != null) {
                    if (newStatus != null && !newStatus.isEmpty()) {
                        complaint.setStatus(newStatus);
                    }
                    if (assignTo != null && !assignTo.isEmpty()) {
                        complaint.setAssignedTo(assignTo);
                    }
                    complaintRepository.save(complaint);
                    updatedCount++;
                }
            }
            
            response.put("status", "success");
            response.put("message", updatedCount + " complaints updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to update complaints");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/note")
    public ResponseEntity<Map<String, String>> addStaffNote(@RequestBody StaffNote note) {
        Map<String, String> response = new HashMap<>();
        
        try {
            staffNoteRepository.save(note);
            
            response.put("status", "success");
            response.put("message", "Note added successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to add note");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/notes/{complaintId}")
    public ResponseEntity<List<StaffNote>> getStaffNotes(@PathVariable String complaintId) {
        try {
            List<StaffNote> notes = staffNoteRepository.findByComplaintIdOrderByCreatedAtDesc(complaintId);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/performance/{email}")
    public ResponseEntity<Map<String, Object>> getStaffPerformance(@PathVariable String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Complaint> allComplaints = complaintRepository.findAll();
            List<Complaint> assignedComplaints = allComplaints.stream()
                .filter(c -> email.equals(c.getAssignedTo()))
                .collect(Collectors.toList());
                
            long totalAssigned = assignedComplaints.size();
            long resolved = assignedComplaints.stream()
                .filter(c -> "RESOLVED".equals(c.getStatus()))
                .count();
            long inProgress = assignedComplaints.stream()
                .filter(c -> "IN_PROGRESS".equals(c.getStatus()))
                .count();
            long pending = assignedComplaints.stream()
                .filter(c -> "PENDING".equals(c.getStatus()))
                .count();
                
            double resolutionRate = totalAssigned > 0 ? (double) resolved / totalAssigned * 100 : 0;
            
            response.put("totalAssigned", totalAssigned);
            response.put("resolved", resolved);
            response.put("inProgress", inProgress);
            response.put("pending", pending);
            response.put("resolutionRate", Math.round(resolutionRate * 100.0) / 100.0);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<Staff>> getAllStaff() {
        try {
            List<Staff> staff = staffRepository.findAll();
            staff.forEach(s -> s.setPassword(null));
            return ResponseEntity.ok(staff);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/init")
    public ResponseEntity<Map<String, String>> initializeStaff() {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Create default staff account if it doesn't exist
            if (!staffRepository.existsByEmail("staff@gmail.com")) {
                Staff staff = new Staff();
                staff.setEmail("staff@gmail.com");
                staff.setPassword(passwordEncoder.encode("staff@123"));
                staff.setFirstName("Support");
                staff.setLastName("Agent");
                staff.setRole("STAFF");
                staff.setDepartment("Customer Support");
                staff.setPhone("1234567890");
                staff.setCreatedAt(LocalDateTime.now());
                staff.setUpdatedAt(LocalDateTime.now());
                staffRepository.save(staff);
                System.out.println("Created staff account: staff@gmail.com");
            } else {
                // Update existing staff with proper names if missing
                Optional<Staff> existingStaff = staffRepository.findByEmail("staff@gmail.com");
                if (existingStaff.isPresent()) {
                    Staff staff = existingStaff.get();
                    if (staff.getFirstName() == null || staff.getFirstName().isEmpty()) {
                        staff.setFirstName("Support");
                        staff.setLastName("Agent");
                        staff.setDepartment("Customer Support");
                        staff.setUpdatedAt(LocalDateTime.now());
                        staffRepository.save(staff);
                        System.out.println("Updated staff account with proper names");
                    }
                }
            }

            // Create default admin account if it doesn't exist
            if (!staffRepository.existsByEmail("admin@gmail.com")) {
                Staff admin = new Staff();
                admin.setEmail("admin@gmail.com");
                admin.setPassword(passwordEncoder.encode("admin@123"));
                admin.setFirstName("System");
                admin.setLastName("Administrator");
                admin.setRole("ADMIN");
                admin.setDepartment("Administration");
                admin.setPhone("0987654321");
                admin.setCreatedAt(LocalDateTime.now());
                admin.setUpdatedAt(LocalDateTime.now());
                staffRepository.save(admin);
                System.out.println("Created admin account: admin@gmail.com");
            } else {
                // Update existing admin with proper names if missing
                Optional<Staff> existingAdmin = staffRepository.findByEmail("admin@gmail.com");
                if (existingAdmin.isPresent()) {
                    Staff admin = existingAdmin.get();
                    if (admin.getFirstName() == null || admin.getFirstName().isEmpty()) {
                        admin.setFirstName("System");
                        admin.setLastName("Administrator");
                        admin.setDepartment("Administration");
                        admin.setUpdatedAt(LocalDateTime.now());
                        staffRepository.save(admin);
                        System.out.println("Updated admin account with proper names");
                    }
                }
            }
            
            response.put("status", "success");
            response.put("message", "Staff accounts initialized");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to initialize staff accounts");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}