package com.example.test_spring.controller;

import com.example.test_spring.model.*;
import com.example.test_spring.repository.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StaffRepository staffRepository;
    
    @Autowired
    private ComplaintRepository complaintRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private SystemSettingRepository systemSettingRepository;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @GetMapping("/dashboard/{email}")
    public ResponseEntity<Map<String, Object>> getAdminDashboard(@PathVariable String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Staff> adminOptional = staffRepository.findByEmail(email);
            Staff admin;
            
            if (adminOptional.isPresent() && "ADMIN".equals(adminOptional.get().getRole())) {
                admin = adminOptional.get();
            } else if ("admin@gmail.com".equals(email)) {
                // Create default admin data for hardcoded account
                admin = new Staff();
                admin.setEmail(email);
                admin.setFirstName("System");
                admin.setLastName("Administrator");
                admin.setRole("ADMIN");
                admin.setDepartment("Administration");
            } else {
                return ResponseEntity.status(403).build();
            }
            
            // Get counts
            long totalUsers = userRepository.count();
            long totalStaff = staffRepository.count();
            long totalComplaints = complaintRepository.count();
            
            // Get resolved complaints today
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            long resolvedToday = complaintRepository.countByStatusAndUpdatedAtBetween("RESOLVED", startOfDay, endOfDay);
            
            // Get recent complaints
            List<Complaint> recentComplaints = complaintRepository.findTop10ByOrderByCreatedAtDesc();
            
            response.put("firstName", admin.getFirstName());
            response.put("lastName", admin.getLastName());
            response.put("email", admin.getEmail());
            response.put("role", admin.getRole());
            response.put("totalUsers", totalUsers);
            response.put("totalStaff", totalStaff);
            response.put("totalComplaints", totalComplaints);
            response.put("resolvedToday", resolvedToday);
            response.put("recentComplaints", recentComplaints);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            // Remove passwords from response
            users.forEach(user -> user.setPassword(null));
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/staff")
    public ResponseEntity<List<Staff>> getAllStaff() {
        try {
            List<Staff> staff = staffRepository.findAll();
            // Remove passwords from response
            staff.forEach(s -> s.setPassword(null));
            return ResponseEntity.ok(staff);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @DeleteMapping("/user/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String id) {
        Map<String, String> response = new HashMap<>();
        
        try {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
                response.put("status", "success");
                response.put("message", "User deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "User not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to delete user");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @DeleteMapping("/staff/{id}")
    public ResponseEntity<Map<String, String>> deleteStaff(@PathVariable String id) {
        Map<String, String> response = new HashMap<>();
        
        try {
            if (staffRepository.existsById(id)) {
                staffRepository.deleteById(id);
                response.put("status", "success");
                response.put("message", "Staff deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Staff not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to delete staff");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/user")
    public ResponseEntity<Map<String, String>> createUser(@RequestBody User user) {
        Map<String, String> response = new HashMap<>();
        
        try {
            if (userRepository.existsByEmail(user.getEmail())) {
                response.put("status", "error");
                response.put("message", "Email already exists");
                return ResponseEntity.badRequest().body(response);
            }
            
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            
            // Log action
            auditLogRepository.save(new AuditLog("admin", "CREATE", "USER", user.getId(), "Created user: " + user.getEmail()));
            
            response.put("status", "success");
            response.put("message", "User created successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to create user");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PutMapping("/user/{id}")
    public ResponseEntity<Map<String, String>> updateUser(@PathVariable String id, @RequestBody User updatedUser) {
        Map<String, String> response = new HashMap<>();
        
        try {
            Optional<User> userOptional = userRepository.findById(id);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setFullName(updatedUser.getFullName());
                user.setPhoneNumber(updatedUser.getPhoneNumber());
                user.setAddress(updatedUser.getAddress());
                
                if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                    user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
                }
                
                userRepository.save(user);
                
                // Log action
                auditLogRepository.save(new AuditLog("admin", "UPDATE", "USER", id, "Updated user: " + user.getEmail()));
                
                response.put("status", "success");
                response.put("message", "User updated successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "User not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to update user");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/staff")
    public ResponseEntity<Map<String, String>> createStaff(@RequestBody Staff staff) {
        Map<String, String> response = new HashMap<>();
        
        try {
            if (staffRepository.existsByEmail(staff.getEmail())) {
                response.put("status", "error");
                response.put("message", "Email already exists");
                return ResponseEntity.badRequest().body(response);
            }
            
            staff.setPassword(passwordEncoder.encode(staff.getPassword()));
            staffRepository.save(staff);
            
            // Log action
            auditLogRepository.save(new AuditLog("admin", "CREATE", "STAFF", staff.getId(), "Created staff: " + staff.getEmail()));
            
            response.put("status", "success");
            response.put("message", "Staff created successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to create staff");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PutMapping("/staff/{id}")
    public ResponseEntity<Map<String, String>> updateStaff(@PathVariable String id, @RequestBody Staff updatedStaff) {
        Map<String, String> response = new HashMap<>();
        
        try {
            Optional<Staff> staffOptional = staffRepository.findById(id);
            if (staffOptional.isPresent()) {
                Staff staff = staffOptional.get();
                staff.setFirstName(updatedStaff.getFirstName());
                staff.setLastName(updatedStaff.getLastName());
                staff.setDepartment(updatedStaff.getDepartment());
                staff.setRole(updatedStaff.getRole());
                
                if (updatedStaff.getPassword() != null && !updatedStaff.getPassword().isEmpty()) {
                    staff.setPassword(passwordEncoder.encode(updatedStaff.getPassword()));
                }
                
                staffRepository.save(staff);
                
                // Log action
                auditLogRepository.save(new AuditLog("admin", "UPDATE", "STAFF", id, "Updated staff: " + staff.getEmail()));
                
                response.put("status", "success");
                response.put("message", "Staff updated successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Staff not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to update staff");
            return ResponseEntity.internalServerError().body(response);
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
        Map<String, String> response = new HashMap<>();
        
        try {
            for (SystemSetting setting : settings) {
                Optional<SystemSetting> existing = systemSettingRepository.findByKey(setting.getKey());
                if (existing.isPresent()) {
                    SystemSetting existingSetting = existing.get();
                    existingSetting.setValue(setting.getValue());
                    systemSettingRepository.save(existingSetting);
                } else {
                    systemSettingRepository.save(setting);
                }
            }
            
            // Log action
            auditLogRepository.save(new AuditLog("admin", "UPDATE", "SYSTEM", "settings", "Updated system settings"));
            
            response.put("status", "success");
            response.put("message", "Settings updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to update settings");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> getReports() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // System overview
            long totalUsers = userRepository.count();
            long totalStaff = staffRepository.count();
            long totalComplaints = complaintRepository.count();
            
            // Complaint statistics
            long pendingComplaints = complaintRepository.countByStatus("PENDING");
            long inProgressComplaints = complaintRepository.countByStatus("IN_PROGRESS");
            long resolvedComplaints = complaintRepository.countByStatus("RESOLVED");
            
            // Recent activity
            List<AuditLog> recentActivity = auditLogRepository.findByOrderByTimestampDesc()
                .stream().limit(10).collect(Collectors.toList());
            
            response.put("totalUsers", totalUsers);
            response.put("totalStaff", totalStaff);
            response.put("totalComplaints", totalComplaints);
            response.put("pendingComplaints", pendingComplaints);
            response.put("inProgressComplaints", inProgressComplaints);
            response.put("resolvedComplaints", resolvedComplaints);
            response.put("recentActivity", recentActivity);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        try {
            List<AuditLog> logs = auditLogRepository.findByOrderByTimestampDesc();
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/backup")
    public ResponseEntity<Map<String, String>> createBackup() {
        Map<String, String> response = new HashMap<>();
        
        try {
            // In a real implementation, this would create actual backups
            String backupId = "backup_" + System.currentTimeMillis();
            
            // Log action
            auditLogRepository.save(new AuditLog("admin", "BACKUP", "SYSTEM", backupId, "System backup created"));
            
            response.put("status", "success");
            response.put("message", "Backup created successfully");
            response.put("backupId", backupId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to create backup");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/complaints")
    public ResponseEntity<List<Complaint>> getAllComplaints() {
        try {
            List<Complaint> complaints = complaintRepository.findByOrderByCreatedAtDesc();
            return ResponseEntity.ok(complaints);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/profile/{email}")
    public ResponseEntity<Staff> getAdminProfile(@PathVariable String email) {
        try {
            Optional<Staff> adminOptional = staffRepository.findByEmail(email);
            if (adminOptional.isPresent() && "ADMIN".equals(adminOptional.get().getRole())) {
                Staff admin = adminOptional.get();
                admin.setPassword(null); // Don't send password
                return ResponseEntity.ok(admin);
            } else if ("admin@gmail.com".equals(email)) {
                // Return default admin data for hardcoded account
                Staff admin = new Staff();
                admin.setEmail(email);
                admin.setFirstName("System");
                admin.setLastName("Administrator");
                admin.setRole("ADMIN");
                admin.setDepartment("Administration");
                return ResponseEntity.ok(admin);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/profile/{email}")
    public ResponseEntity<Map<String, String>> updateAdminProfile(
            @PathVariable String email, 
            @RequestBody Map<String, String> updatedData) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            Optional<Staff> adminOptional = staffRepository.findByEmail(email);
            if (adminOptional.isPresent() && "ADMIN".equals(adminOptional.get().getRole())) {
                Staff admin = adminOptional.get();
                
                // Update profile data
                if (updatedData.containsKey("firstName")) {
                    admin.setFirstName(updatedData.get("firstName"));
                }
                if (updatedData.containsKey("lastName")) {
                    admin.setLastName(updatedData.get("lastName"));
                }
                if (updatedData.containsKey("phone")) {
                    admin.setPhone(updatedData.get("phone"));
                }
                if (updatedData.containsKey("department")) {
                    admin.setDepartment(updatedData.get("department"));
                }
                
                admin.setUpdatedAt(LocalDateTime.now());
                staffRepository.save(admin);
                
                // Log action
                auditLogRepository.save(new AuditLog(email, "UPDATE", "ADMIN_PROFILE", admin.getId(), "Updated admin profile"));
                
                response.put("status", "success");
                response.put("message", "Profile updated successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Admin not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to update profile");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}