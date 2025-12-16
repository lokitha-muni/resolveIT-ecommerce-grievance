package com.example.test_spring.service;

import com.example.test_spring.dto.LoginRequest;
import com.example.test_spring.dto.RegisterRequest;
import com.example.test_spring.dto.ProfileUpdateRequest;
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
    
    // Get user by email
    public User getUserByEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        return userOptional.orElse(null);
    }
    
    // Update user profile
    public String updateUserProfile(String email, ProfileUpdateRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            return "User not found";
        }
        
        User user = userOptional.get();
        
        // Update profile fields
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            user.setCity(request.getCity());
        }
        if (request.getState() != null) {
            user.setState(request.getState());
        }
        if (request.getZipCode() != null) {
            user.setZipCode(request.getZipCode());
        }
        if (request.getCountry() != null) {
            user.setCountry(request.getCountry());
        }
        
        // Update notification preferences
        user.setEmailNotifications(request.isEmailNotifications());
        user.setSmsNotifications(request.isSmsNotifications());
        user.setMarketingEmails(request.isMarketingEmails());
        
        // Update password if provided
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isEmpty()) {
                return "Current password is required to change password";
            }
            
            // Verify current password
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                return "Current password is incorrect";
            }
            
            // Update password
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }
        
        // Save updated user
        userRepository.save(user);
        
        return "Profile updated successfully";
    }
}