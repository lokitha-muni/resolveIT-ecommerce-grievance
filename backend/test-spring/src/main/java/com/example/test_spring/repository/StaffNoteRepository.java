package com.example.test_spring.repository;

import com.example.test_spring.model.StaffNote;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffNoteRepository extends MongoRepository<StaffNote, String> {
    List<StaffNote> findByComplaintIdOrderByCreatedAtDesc(String complaintId);
    List<StaffNote> findByStaffEmailOrderByCreatedAtDesc(String staffEmail);
}