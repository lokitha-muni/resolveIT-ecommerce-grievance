package com.example.test_spring.controller;

import com.example.test_spring.model.Complaint;
import com.example.test_spring.model.Staff;
import com.example.test_spring.model.User;
import com.example.test_spring.model.AuditLog;
import com.example.test_spring.repository.ComplaintRepository;
import com.example.test_spring.repository.StaffRepository;
import com.example.test_spring.repository.UserRepository;
import com.example.test_spring.repository.AuditLogRepository;
import com.example.test_spring.repository.CommentRepository;
import com.example.test_spring.repository.StaffNoteRepository;
import com.example.test_spring.repository.RatingRepository;
import com.example.test_spring.repository.NotificationRepository;
import com.example.test_spring.model.SystemSetting;
import com.example.test_spring.repository.SystemSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private StaffRepository staffRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SystemSettingRepository systemSettingRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private StaffNoteRepository staffNoteRepository;
    
    @Autowired
    private RatingRepository ratingRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/complaints")
    public ResponseEntity<List<Complaint>> getAllComplaints() {
        try {
            List<Complaint> complaints = complaintRepository.findAll();
            return ResponseEntity.ok(complaints);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getAdminDashboard() {
        try {
            List<User> allUsers = userRepository.findAll();
            List<Staff> allStaff = staffRepository.findAll();
            List<Complaint> allComplaints = complaintRepository.findAll();
            
            long totalUsers = allUsers.size();
            long totalStaff = allStaff.size();
            long totalComplaints = allComplaints.size();
            long pendingComplaints = allComplaints.stream().filter(c -> "PENDING".equals(c.getStatus())).count();
            long inProgressComplaints = allComplaints.stream().filter(c -> "IN_PROGRESS".equals(c.getStatus())).count();
            long resolvedComplaints = allComplaints.stream().filter(c -> "RESOLVED".equals(c.getStatus())).count();
            long unassignedComplaints = allComplaints.stream().filter(c -> c.getAssignedTo() == null || c.getAssignedTo().isEmpty()).count();
            long activeAgents = allStaff.stream().filter(s -> s.getWorkload() > 0).count();
            
            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("totalUsers", totalUsers);
            dashboard.put("totalStaff", totalStaff);
            dashboard.put("totalComplaints", totalComplaints);
            dashboard.put("pendingComplaints", pendingComplaints);
            dashboard.put("inProgressComplaints", inProgressComplaints);
            dashboard.put("resolvedComplaints", resolvedComplaints);
            dashboard.put("unassignedComplaints", unassignedComplaints);
            dashboard.put("activeAgents", activeAgents);
            dashboard.put("avgResolutionTime", totalComplaints > 0 ? "2.3h" : "0h");
            
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/settings")
    public ResponseEntity<List<SystemSetting>> getSettings() {
        try {
            List<SystemSetting> settings = systemSettingRepository.findAll();
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/settings")
    public ResponseEntity<Map<String, String>> updateSettings(@RequestBody List<SystemSetting> settings) {
        try {
            for (SystemSetting setting : settings) {
                Optional<SystemSetting> existing = systemSettingRepository.findByKey(setting.getKey());
                if (existing.isPresent()) {
                    SystemSetting existingSetting = existing.get();
                    existingSetting.setValue(setting.getValue());
                    existingSetting.setDescription(setting.getDescription());
                    existingSetting.setCategory(setting.getCategory());
                    existingSetting.setUpdatedAt(java.time.LocalDateTime.now());
                    systemSettingRepository.save(existingSetting);
                } else {
                    setting.setUpdatedAt(java.time.LocalDateTime.now());
                    systemSettingRepository.save(setting);
                }
            }
            return ResponseEntity.ok(Map.of("status", "success", "message", "Settings updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @PutMapping("/complaints/{id}/priority")
    public ResponseEntity<Map<String, String>> updateComplaintPriority(
            @PathVariable String id, 
            @RequestBody Map<String, String> priorityData) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            Optional<Complaint> complaintOptional = complaintRepository.findByComplaintId(id);
            if (complaintOptional.isPresent()) {
                Complaint complaint = complaintOptional.get();
                String oldPriority = complaint.getPriority();
                String newPriority = priorityData.get("priority");
                
                complaint.setPriority(newPriority);
                complaint.setUpdatedAt(java.time.LocalDateTime.now());
                complaintRepository.save(complaint);
                
                // Save audit log
                AuditLog auditLog = new AuditLog(
                    "admin@gmail.com", 
                    "PRIORITY_CHANGE", 
                    "COMPLAINT", 
                    id, 
                    "Priority changed from " + oldPriority + " to " + newPriority + " by Admin"
                );
                auditLogRepository.save(auditLog);
                
                response.put("status", "success");
                response.put("message", "Priority updated successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Complaint not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to update priority");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PutMapping("/complaints/{id}/assign")
    public ResponseEntity<Map<String, String>> assignComplaintToAgent(
            @PathVariable String id, 
            @RequestBody Map<String, String> assignmentData) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            Optional<Complaint> complaintOptional = complaintRepository.findByComplaintId(id);
            if (complaintOptional.isPresent()) {
                Complaint complaint = complaintOptional.get();
                String agentEmail = assignmentData.get("agentEmail");
                String agentName = assignmentData.get("agentName");
                
                complaint.setAssignedTo(agentEmail);
                complaint.setAssignedAgent(agentName);
                complaint.setUpdatedAt(java.time.LocalDateTime.now());
                complaintRepository.save(complaint);
                
                // Save audit log
                AuditLog auditLog = new AuditLog(
                    "admin@gmail.com", 
                    "AGENT_ASSIGNMENT", 
                    "COMPLAINT", 
                    id, 
                    "Complaint assigned to " + agentName + " by Admin"
                );
                auditLogRepository.save(auditLog);
                
                response.put("status", "success");
                response.put("message", "Agent assigned successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Complaint not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to assign agent");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/complaints/{id}/history")
    public ResponseEntity<List<AuditLog>> getComplaintHistory(@PathVariable String id) {
        try {
            List<AuditLog> history = auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc("COMPLAINT", id);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/complaints/{id}/note")
    public ResponseEntity<Map<String, String>> addAdminNote(
            @PathVariable String id, 
            @RequestBody Map<String, String> noteData) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            String note = noteData.get("note");
            
            // Save audit log for admin note
            AuditLog auditLog = new AuditLog(
                "admin@gmail.com", 
                "ADMIN_NOTE", 
                "COMPLAINT", 
                id, 
                "Admin note added: " + note
            );
            auditLogRepository.save(auditLog);
            
            response.put("status", "success");
            response.put("message", "Admin note saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to save admin note");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PutMapping("/complaints/{id}/status")
    public ResponseEntity<Map<String, String>> updateComplaintStatus(
            @PathVariable String id, 
            @RequestBody Map<String, String> statusData) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            Optional<Complaint> complaintOptional = complaintRepository.findByComplaintId(id);
            if (complaintOptional.isPresent()) {
                Complaint complaint = complaintOptional.get();
                String oldStatus = complaint.getStatus();
                String newStatus = statusData.get("status");
                
                complaint.setStatus(newStatus);
                complaint.setUpdatedAt(java.time.LocalDateTime.now());
                complaintRepository.save(complaint);
                
                // Save audit log
                AuditLog auditLog = new AuditLog(
                    "admin@gmail.com", 
                    "STATUS_CHANGE", 
                    "COMPLAINT", 
                    id, 
                    "Status updated from " + oldStatus + " to " + newStatus + " by Admin"
                );
                auditLogRepository.save(auditLog);
                
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

    @DeleteMapping("/users/{email}")
    @Transactional
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String email) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // 1. Get all complaints by this user
            List<Complaint> userComplaints = complaintRepository.findAll().stream()
                .filter(c -> email.equals(c.getUserEmail()))
                .collect(Collectors.toList());
            
            // 2. Delete all related data for each complaint
            for (Complaint complaint : userComplaints) {
                String complaintId = complaint.getComplaintId();
                
                // Delete comments
                commentRepository.deleteByComplaintId(complaintId);
                
                // Delete staff notes
                staffNoteRepository.deleteByComplaintId(complaintId);
                
                // Delete ratings
                ratingRepository.deleteByComplaintId(complaintId);
                
                // Delete audit logs
                auditLogRepository.deleteByEntityTypeAndEntityId("COMPLAINT", complaintId);
            }
            
            // 3. Delete all complaints by this user
            complaintRepository.deleteAll(userComplaints);
            
            // 4. Delete user notifications
            notificationRepository.deleteByUserEmail(email);
            
            // 5. Delete user audit logs
            auditLogRepository.deleteByUserEmail(email);
            
            // 6. Delete the user account
            userRepository.deleteByEmail(email);
            
            response.put("status", "success");
            response.put("message", "User and all related data deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to delete user: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/staff/{email}")
    @Transactional
    public ResponseEntity<Map<String, String>> deleteStaff(@PathVariable String email) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Prevent deletion of admin accounts
            Optional<Staff> staffOptional = staffRepository.findByEmail(email);
            if (staffOptional.isPresent() && "ADMIN".equals(staffOptional.get().getRole())) {
                response.put("status", "error");
                response.put("message", "Cannot delete admin accounts");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 1. Get all complaints assigned to this staff member
            List<Complaint> assignedComplaints = complaintRepository.findAll().stream()
                .filter(c -> email.equals(c.getAssignedTo()))
                .collect(Collectors.toList());
            
            // 2. Unassign complaints (set to null)
            for (Complaint complaint : assignedComplaints) {
                complaint.setAssignedTo(null);
                complaint.setAssignedAgent(null);
                complaint.setUpdatedAt(java.time.LocalDateTime.now());
                complaintRepository.save(complaint);
                
                // Add audit log for unassignment
                AuditLog auditLog = new AuditLog(
                    "admin@gmail.com", 
                    "STAFF_DELETED", 
                    "COMPLAINT", 
                    complaint.getComplaintId(), 
                    "Staff member " + email + " deleted, complaint unassigned"
                );
                auditLogRepository.save(auditLog);
            }
            
            // 3. Delete staff notes by this staff member
            staffNoteRepository.deleteByStaffEmail(email);
            
            // 4. Delete audit logs by this staff member
            auditLogRepository.deleteByUserEmail(email);
            
            // 5. Delete the staff account
            staffRepository.deleteByEmail(email);
            
            response.put("status", "success");
            response.put("message", "Staff member and all related data deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to delete staff member: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/staff")
    public ResponseEntity<Map<String, String>> createStaff(@RequestBody Map<String, String> staffData) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String email = staffData.get("email");
            String firstName = staffData.get("firstName");
            String lastName = staffData.get("lastName");
            String password = staffData.get("password");
            String department = staffData.get("department");
            String phone = staffData.get("phone");
            
            // Check if staff already exists
            if (staffRepository.existsByEmail(email)) {
                response.put("status", "error");
                response.put("message", "Staff member with this email already exists");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create new staff member
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            Staff staff = new Staff();
            staff.setEmail(email);
            staff.setPassword(passwordEncoder.encode(password));
            staff.setFirstName(firstName);
            staff.setLastName(lastName);
            staff.setRole("STAFF");
            staff.setDepartment(department);
            staff.setPhone(phone);
            staff.setWorkload(0);
            
            staffRepository.save(staff);
            
            response.put("status", "success");
            response.put("message", "Staff member created successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to create staff member: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/init-data")
    public ResponseEntity<Map<String, String>> initData() {
        try {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            
            // Create 5 sample users
            String[] userEmails = {"john.doe@gmail.com", "jane.smith@gmail.com", "mike.johnson@gmail.com", "sarah.wilson@gmail.com", "david.brown@gmail.com"};
            String[] firstNames = {"John", "Jane", "Mike", "Sarah", "David"};
            String[] lastNames = {"Doe", "Smith", "Johnson", "Wilson", "Brown"};
            
            int usersCreated = 0;
            for (int i = 0; i < userEmails.length; i++) {
                if (!userRepository.existsByEmail(userEmails[i])) {
                    User user = new User();
                    user.setEmail(userEmails[i]);
                    user.setPassword(passwordEncoder.encode("password123"));
                    user.setFirstName(firstNames[i]);
                    user.setLastName(lastNames[i]);
                    user.setPhone("987654321" + i);
                    userRepository.save(user);
                    usersCreated++;
                }
            }
            
            // Create 5 sample staff members
            String[] staffEmails = {"agent1@resolveit.com", "agent2@resolveit.com", "agent3@resolveit.com", "agent4@resolveit.com", "agent5@resolveit.com"};
            String[] staffFirstNames = {"Alex", "Emma", "Ryan", "Lisa", "Mark"};
            String[] staffLastNames = {"Garcia", "Davis", "Miller", "Anderson", "Taylor"};
            
            int staffCreated = 0;
            for (int i = 0; i < staffEmails.length; i++) {
                if (!staffRepository.existsByEmail(staffEmails[i])) {
                    Staff staff = new Staff();
                    staff.setEmail(staffEmails[i]);
                    staff.setPassword(passwordEncoder.encode("staff123"));
                    staff.setFirstName(staffFirstNames[i]);
                    staff.setLastName(staffLastNames[i]);
                    staff.setRole("STAFF");
                    staff.setDepartment("Customer Support");
                    staff.setWorkload(i + 1);
                    staffRepository.save(staff);
                    staffCreated++;
                }
            }
            
            // Create 5 sample complaints
            String[] issueTypes = {"Late Delivery", "Damaged Package", "Wrong Item", "Missing Item", "Poor Service"};
            String[] descriptions = {
                "Package was delivered 3 days late",
                "Item arrived with visible damage", 
                "Received different product than ordered",
                "One item missing from the order",
                "Delivery person was rude and unprofessional"
            };
            String[] statuses = {"PENDING", "IN_PROGRESS", "RESOLVED", "PENDING", "IN_PROGRESS"};
            
            int complaintsCreated = 0;
            for (int i = 0; i < userEmails.length; i++) {
                String complaintId = "CMP-" + String.format("%04d", i + 1);
                if (!complaintRepository.existsByComplaintId(complaintId)) {
                    Complaint complaint = new Complaint();
                    complaint.setComplaintId(complaintId);
                    complaint.setUserEmail(userEmails[i]);
                    complaint.setOrderId("ORD-" + String.format("%06d", 100001 + i));
                    complaint.setIssueType(issueTypes[i]);
                    complaint.setDescription(descriptions[i]);
                    complaint.setStatus(statuses[i]);
                    complaint.setPriority("Medium");
                    complaint.setAssignedTo(staffEmails[i]);
                    complaint.setAssignedAgent(staffFirstNames[i] + " " + staffLastNames[i]);
                    complaintRepository.save(complaint);
                    complaintsCreated++;
                }
            }
            
            return ResponseEntity.ok(Map.of("status", "success", "message", 
                "Created " + usersCreated + " users, " + staffCreated + " staff, and " + complaintsCreated + " complaints"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}