package com.example.test_spring.repository;

import com.example.test_spring.model.Rating;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends MongoRepository<Rating, String> {
    Optional<Rating> findByComplaintId(String complaintId);
    boolean existsByComplaintId(String complaintId);
    void deleteByComplaintId(String complaintId);
    List<Rating> findByStaffEmail(String staffEmail);
}