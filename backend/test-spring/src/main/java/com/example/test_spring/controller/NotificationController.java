package com.example.test_spring.controller;

import com.example.test_spring.model.Notification;
import com.example.test_spring.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable String id) {
        Map<String, String> response = new HashMap<>();
        
        try {
            Optional<Notification> notificationOpt = notificationRepository.findById(id);
            if (notificationOpt.isPresent()) {
                Notification notification = notificationOpt.get();
                notification.setRead(true);
                notificationRepository.save(notification);
                
                response.put("status", "success");
                response.put("message", "Notification marked as read");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Notification not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to mark notification as read");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PutMapping("/mark-all-read/{userEmail}")
    public ResponseEntity<Map<String, String>> markAllAsRead(@PathVariable String userEmail) {
        Map<String, String> response = new HashMap<>();
        
        try {
            List<Notification> notifications = notificationRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
            for (Notification notification : notifications) {
                if (!notification.isRead()) {
                    notification.setRead(true);
                    notificationRepository.save(notification);
                }
            }
            
            response.put("status", "success");
            response.put("message", "All notifications marked as read");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to mark notifications as read");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}