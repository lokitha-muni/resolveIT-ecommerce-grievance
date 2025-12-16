package com.example.test_spring.repository;

import com.example.test_spring.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByComplaintIdOrderByCreatedAtAsc(String complaintId);
}