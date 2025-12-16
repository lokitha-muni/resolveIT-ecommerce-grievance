package com.example.test_spring.controller;

import com.example.test_spring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class DatabaseTestController {

    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/mongodb")
    public ResponseEntity<Map<String, Object>> testMongoConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test MongoDB connection
            String dbName = mongoTemplate.getDb().getName();
            long userCount = userRepository.count();
            
            response.put("status", "SUCCESS");
            response.put("message", "MongoDB connection successful");
            response.put("database", dbName);
            response.put("userCount", userCount);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "MongoDB connection failed: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}