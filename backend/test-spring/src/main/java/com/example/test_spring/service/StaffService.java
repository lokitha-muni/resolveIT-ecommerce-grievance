package com.example.test_spring.service;

import com.example.test_spring.model.Staff;
import com.example.test_spring.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class StaffService {
    
    @Autowired
    private StaffRepository staffRepository;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public void initializeDefaultAccounts() {
        // Create default staff account
        if (!staffRepository.existsByEmail("staff@gmail.com")) {
            Staff staff = new Staff();
            staff.setEmail("staff@gmail.com");
            staff.setPassword(passwordEncoder.encode("staff@123"));
            staff.setFirstName("Staff");
            staff.setLastName("User");
            staff.setRole("STAFF");
            staff.setDepartment("Customer Support");
            staffRepository.save(staff);
            System.out.println("Default staff account created: staff@gmail.com / staff@123");
        }
        
        // Create default admin account
        if (!staffRepository.existsByEmail("admin@gmail.com")) {
            Staff admin = new Staff();
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("admin@123"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setRole("ADMIN");
            admin.setDepartment("Administration");
            staffRepository.save(admin);
            System.out.println("Default admin account created: admin@gmail.com / admin@123");
        }
    }
}