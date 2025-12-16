package com.example.test_spring.repository;

import com.example.test_spring.model.Staff;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends MongoRepository<Staff, String> {
    Optional<Staff> findByEmail(String email);
    boolean existsByEmail(String email);
}