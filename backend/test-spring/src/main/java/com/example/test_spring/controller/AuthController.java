package com.example.test_spring.controller;

import com.example.test_spring.dto.LoginRequest;
import com.example.test_spring.dto.RegisterRequest;
import com.example.test_spring.dto.ProfileUpdateRequest;
import com.example.test_spring.service.AuthService;
import com.example.test_spring.service.EmailService;
import com.example.test_spring.model.User;
import com.example.test_spring.repository.UserRepository;
import com.example.test_spring.security.*;
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
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private InputSanitizer inputSanitizer;
    
    @Autowired
    private SessionManager sessionManager;
    
    @Autowired
    private TwoFactorService twoFactorService;
    
    @Autowired
    private EmailService emailService;
    
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
    
    // Login endpoint with JWT
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Sanitize input
            String email = inputSanitizer.sanitizeHtml(request.getEmail());
            
            if (!inputSanitizer.isValidEmail(email)) {
                response.put("status", "error");
                response.put("message", "Invalid email format");
                return ResponseEntity.badRequest().body(response);
            }
            
            String result = authService.loginUser(request);
            
            if (result.equals("Login successful")) {
                // Generate JWT token
                String token = jwtUtil.generateToken(email, "USER");
                sessionManager.createSession(token);
                
                response.put("status", "success");
                response.put("message", result);
                response.put("token", token);
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
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authHeader) {
        Map<String, String> response = new HashMap<>();
        
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                sessionManager.invalidateSession(token);
            }
            
            response.put("status", "success");
            response.put("message", "Logged out successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "success");
            response.put("message", "Logged out successfully");
            return ResponseEntity.ok(response);
        }
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String email = inputSanitizer.sanitizeHtml(request.get("email"));
            
            if (!inputSanitizer.isValidEmail(email)) {
                response.put("status", "error");
                response.put("message", "Invalid email format");
                return ResponseEntity.badRequest().body(response);
            }
            
            Optional<User> userOptional = userRepository.findByEmail(email);
            
            if (userOptional.isPresent()) {
                response.put("status", "success");
                response.put("message", "User verified. Frontend will send OTP via EmailJS.");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "No account found with this email. Please register first.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to process request: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/store-otp")
    public ResponseEntity<Map<String, String>> storeOTP(@RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String email = inputSanitizer.sanitizeHtml(request.get("email"));
            String otp = request.get("otp");
            
            // Store OTP in MongoDB via TwoFactorService
            twoFactorService.storeOTP(email, otp);
            
            response.put("status", "success");
            response.put("message", "OTP stored successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to store OTP");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    
    @PostMapping("/verify-reset-otp")
    public ResponseEntity<Map<String, String>> verifyResetOTP(@RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String email = inputSanitizer.sanitizeHtml(request.get("email"));
            String otp = request.get("otp");
            
            if (twoFactorService.verifyOTP(email, otp)) {
                response.put("status", "success");
                response.put("message", "OTP verified successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Invalid or expired OTP");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "OTP verification failed");
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
    
    // Staff login without 2FA
    @PostMapping("/staff/login")
    public ResponseEntity<Map<String, String>> staffLogin(@RequestBody LoginRequest request) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String email = inputSanitizer.sanitizeHtml(request.getEmail());
            
            // Check staff credentials (hardcoded for demo)
            if ((email.equals("staff@gmail.com") && request.getPassword().equals("staff@123")) ||
                (email.equals("admin@gmail.com") && request.getPassword().equals("admin@123"))) {
                
                String role = email.equals("admin@gmail.com") ? "ADMIN" : "STAFF";
                String token = jwtUtil.generateToken(email, role);
                sessionManager.createSession(token);
                
                response.put("status", "success");
                response.put("message", "Login successful");
                response.put("token", token);
                response.put("role", role);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Invalid credentials");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/staff/verify-otp")
    public ResponseEntity<Map<String, String>> verifyStaffOTP(@RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String email = inputSanitizer.sanitizeHtml(request.get("email"));
            String otp = request.get("otp");
            
            if (twoFactorService.verifyOTP(email, otp)) {
                String role = email.equals("admin@gmail.com") ? "ADMIN" : "STAFF";
                String token = jwtUtil.generateToken(email, role);
                sessionManager.createSession(token);
                
                response.put("status", "success");
                response.put("message", "Login successful");
                response.put("token", token);
                response.put("role", role);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Invalid or expired OTP");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "OTP verification failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/init-data")
    public ResponseEntity<Map<String, String>> initData() {
        return ResponseEntity.ok(Map.of("status", "success", "message", "Data initialization working"));
    }

    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, String>> validateToken(@RequestHeader("Authorization") String authHeader) {
        Map<String, String> response = new HashMap<>();
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("status", "error");
                response.put("message", "Invalid token format");
                return ResponseEntity.badRequest().body(response);
            }
            
            String token = authHeader.substring(7);
            
            if (!sessionManager.isSessionValid(token)) {
                response.put("status", "error");
                response.put("message", "Session expired");
                return ResponseEntity.badRequest().body(response);
            }
            
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);
            
            if (jwtUtil.validateToken(token, email)) {
                sessionManager.updateSession(token);
                
                response.put("status", "success");
                response.put("email", email);
                response.put("role", role);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Invalid token");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Token validation failed");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}