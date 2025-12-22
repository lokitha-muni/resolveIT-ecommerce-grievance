package com.example.test_spring.repository;

import com.example.test_spring.model.Complaint;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface ComplaintRepository extends MongoRepository<Complaint, String> {
    
    List<Complaint> findByUserEmailOrderByUpdatedAtDesc(String userEmail);
    List<Complaint> findByUserEmailOrderByCreatedAtDesc(String userEmail);
    
    List<Complaint> findByUserEmail(String userEmail);
    List<Complaint> findByUserEmailAndStatus(String userEmail, String status);
    List<Complaint> findByUserEmailAndIssueType(String userEmail, String issueType);
    List<Complaint> findByUserEmailAndStatusAndIssueType(String userEmail, String status, String issueType);
    
    long countByUserEmailAndStatus(String userEmail, String status);
    long countByUserEmail(String userEmail);
    
    List<Complaint> findByOrderByCreatedAtDesc();
    List<Complaint> findTop10ByOrderByCreatedAtDesc();
    List<Complaint> findByAssignedTo(String assignedTo);
    List<Complaint> findByAssignedToOrderByCreatedAtDesc(String assignedTo);
    
    long countByStatus(String status);
    long countByStatusAndUpdatedAtBetween(String status, LocalDateTime start, LocalDateTime end);
    long countByAssignedToAndStatus(String assignedTo, String status);
    
    java.util.Optional<Complaint> findByComplaintId(String complaintId);
    boolean existsByComplaintId(String complaintId);
}