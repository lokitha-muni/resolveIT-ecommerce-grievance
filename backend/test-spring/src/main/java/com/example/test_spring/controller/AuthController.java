package com.example.test_spring.controller;

import com.example.test_spring.dto.LoginRequest;
import com.example.test_spring.dto.RegisterRequest;
import com.example.test_spring.dto.ProfileUpdateRequest;
import com.example.test_spring.service.AuthService;
import com.example.test_spring.model.User;
import com.example.test_spring.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Allow all origins for development
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserRepository userRepository;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    // Register endpoint
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        Map<String, String> response = new HashMap<>();
        
        try {
            System.out.println("Registration request received: " + request.getEmail());
            String result = authService.registerUser(request);
            System.out.println("Registration result: " + result);
            
            if (result.equals("User registered successfully")) {
                response.put("status", "success");
                response.put("message", result);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", result);
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // Test endpoint to check users
    @GetMapping("/users")
    public ResponseEntity<Object> getAllUsers() {
        try {
            return ResponseEntity.ok(authService.getAllUsers());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    
    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String result = authService.loginUser(request);
            
            if (result.equals("Login successful")) {
                response.put("status", "success");
                response.put("message", result);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", result);
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // Get user profile
    @GetMapping("/profile/{email}")
    public ResponseEntity<User> getProfile(@PathVariable String email) {
        try {
            User user = authService.getUserByEmail(email);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Update user profile
    @PutMapping("/profile/{email}")
    public ResponseEntity<Map<String, String>> updateProfile(
            @PathVariable String email,
            @RequestBody ProfileUpdateRequest request) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            String result = authService.updateUserProfile(email, request);
            
            if (result.equals("Profile updated successfully")) {
                response.put("status", "success");
                response.put("message", result);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", result);
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Profile update failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            Optional<User> userOptional = userRepository.findByEmail(email);
            
            if (userOptional.isPresent()) {
                // In a real app, send email with reset token
                // For demo, we'll just return success
                response.put("status", "success");
                response.put("message", "Password reset instructions sent to your email");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Email not found");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to process request");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            String newPassword = request.get("newPassword");
            
            Optional<User> userOptional = userRepository.findByEmail(email);
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                
                response.put("status", "success");
                response.put("message", "Password reset successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "User not found");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to reset password");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            
            Optional<User> userOptional = userRepository.findByEmail(email);
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setEmailVerified(true);
                userRepository.save(user);
                
                response.put("status", "success");
                response.put("message", "Email verified successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "User not found");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to verify email");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}