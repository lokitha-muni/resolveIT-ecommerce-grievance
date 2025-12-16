package com.example.test_spring.controller;

import com.example.test_spring.model.Rating;
import com.example.test_spring.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/ratings")
@CrossOrigin(origins = "*")
public class RatingController {
    
    @Autowired
    private RatingRepository ratingRepository;
    
    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submitRating(@RequestBody Rating rating) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Check if rating already exists
            if (ratingRepository.existsByComplaintId(rating.getComplaintId())) {
                response.put("status", "error");
                response.put("message", "Rating already submitted for this complaint");
                return ResponseEntity.badRequest().body(response);
            }
            
            ratingRepository.save(rating);
            
            response.put("status", "success");
            response.put("message", "Rating submitted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to submit rating");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/complaint/{complaintId}")
    public ResponseEntity<Rating> getRating(@PathVariable String complaintId) {
        try {
            Optional<Rating> rating = ratingRepository.findByComplaintId(complaintId);
            if (rating.isPresent()) {
                return ResponseEntity.ok(rating.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}