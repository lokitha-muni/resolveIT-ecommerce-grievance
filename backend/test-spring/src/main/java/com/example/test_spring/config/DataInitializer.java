package com.example.test_spring.config;

import com.example.test_spring.model.Staff;
import com.example.test_spring.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private StaffRepository staffRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) throws Exception {
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
            staffRepository.save(staff);
            System.out.println("Default staff account created: staff@gmail.com / staff@123");
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
            staffRepository.save(admin);
            System.out.println("Default admin account created: admin@gmail.com / admin@123");
        }
    }
}