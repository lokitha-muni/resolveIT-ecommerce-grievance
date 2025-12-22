package com.example.test_spring.config;

import com.example.test_spring.model.Staff;
import com.example.test_spring.model.User;
import com.example.test_spring.repository.StaffRepository;
import com.example.test_spring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private StaffRepository staffRepository;
    
    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultAccounts();
    }
    
    public void initializeDefaultAccounts() {
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
    
    public void initializeSampleData() {
        // Create 5 sample users
        String[] userEmails = {"john.doe@gmail.com", "jane.smith@gmail.com", "mike.johnson@gmail.com", "sarah.wilson@gmail.com", "david.brown@gmail.com"};
        String[] firstNames = {"John", "Jane", "Mike", "Sarah", "David"};
        String[] lastNames = {"Doe", "Smith", "Johnson", "Wilson", "Brown"};
        String[] phones = {"9876543210", "9876543211", "9876543212", "9876543213", "9876543214"};
        
        for (int i = 0; i < userEmails.length; i++) {
            if (!userRepository.existsByEmail(userEmails[i])) {
                User user = new User();
                user.setEmail(userEmails[i]);
                user.setPassword(passwordEncoder.encode("password123"));
                user.setFirstName(firstNames[i]);
                user.setLastName(lastNames[i]);
                user.setPhone(phones[i]);
                user.setAddress("123 Main St, City " + (i + 1));
                userRepository.save(user);
                System.out.println("Sample user created: " + userEmails[i] + " / password123");
            }
        }
        
        // Create 5 sample staff members
        String[] staffEmails = {"agent1@resolveit.com", "agent2@resolveit.com", "agent3@resolveit.com", "supervisor@resolveit.com", "manager@resolveit.com"};
        String[] staffFirstNames = {"Alex", "Emma", "Ryan", "Lisa", "Mark"};
        String[] staffLastNames = {"Garcia", "Davis", "Miller", "Anderson", "Taylor"};
        String[] departments = {"Customer Support", "Customer Support", "Technical Support", "Quality Assurance", "Management"};
        String[] roles = {"STAFF", "STAFF", "STAFF", "SUPERVISOR", "MANAGER"};
        String[] staffPhones = {"5551234567", "5551234568", "5551234569", "5551234570", "5551234571"};
        
        for (int i = 0; i < staffEmails.length; i++) {
            if (!staffRepository.existsByEmail(staffEmails[i])) {
                Staff staff = new Staff();
                staff.setEmail(staffEmails[i]);
                staff.setPassword(passwordEncoder.encode("staff123"));
                staff.setFirstName(staffFirstNames[i]);
                staff.setLastName(staffLastNames[i]);
                staff.setRole(roles[i]);
                staff.setDepartment(departments[i]);
                staff.setPhone(staffPhones[i]);
                staff.setWorkload(i * 2); // Different workloads
                staffRepository.save(staff);
                System.out.println("Sample staff created: " + staffEmails[i] + " / staff123");
            }
        }
    }
}