package com.example.test_spring.controller;

import com.example.test_spring.model.Complaint;
import com.example.test_spring.model.Staff;
import com.example.test_spring.model.User;
import com.example.test_spring.repository.ComplaintRepository;
import com.example.test_spring.repository.StaffRepository;
import com.example.test_spring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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