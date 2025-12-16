package com.example.test_spring.controller;

import com.example.test_spring.model.Comment;
import com.example.test_spring.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*")
public class CommentController {
    
    @Autowired
    private CommentRepository commentRepository;
    
    @GetMapping("/complaint/{complaintId}")
    public ResponseEntity<List<Comment>> getComments(@PathVariable String complaintId) {
        try {
            List<Comment> comments = commentRepository.findByComplaintIdOrderByCreatedAtAsc(complaintId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addComment(@RequestBody Comment comment) {
        Map<String, String> response = new HashMap<>();
        
        try {
            commentRepository.save(comment);
            
            response.put("status", "success");
            response.put("message", "Comment added successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to add comment");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}