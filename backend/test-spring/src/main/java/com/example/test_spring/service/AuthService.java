package com.example.test_spring.service;

import com.example.test_spring.dto.LoginRequest;
import com.example.test_spring.dto.RegisterRequest;
import com.example.test_spring.model.User;
import com.example.test_spring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Register new user
    public String registerUser(RegisterRequest request) {
        // Validate input
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            return "Full name is required";
        }
        
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return "Email is required";
        }
        
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            return "Password must be at least 6 characters";
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return "Email already exists";
        }
        
        // Create new user
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash password
        user.setPhoneNumber(request.getPhoneNumber());
        
        // Save user
        User savedUser = userRepository.save(user);
        System.out.println("User saved with ID: " + savedUser.getId());
        
        return "User registered successfully";
    }
    
    // Login user
    public String loginUser(LoginRequest request) {
        // Validate input
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return "Email is required";
        }
        
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return "Password is required";
        }
        
        // Find user by email
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        System.out.println("Looking for user with email: " + request.getEmail());
        
        if (userOptional.isEmpty()) {
            System.out.println("User not found in database");
            return "Invalid email or password";
        }
        
        User user = userOptional.get();
        System.out.println("User found: " + user.getEmail());
        System.out.println("Stored password hash: " + user.getPassword());
        System.out.println("Input password: " + request.getPassword());
        
        // Check password
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        System.out.println("Password matches: " + passwordMatches);
        
        if (!passwordMatches) {
            return "Invalid email or password";
        }
        
        return "Login successful";
    }
    
    // Test method to get all users
    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }
}