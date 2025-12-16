package com.example.test_spring.repository;

import com.example.test_spring.model.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    List<AuditLog> findByOrderByTimestampDesc();
    List<AuditLog> findByUserEmailOrderByTimestampDesc(String userEmail);
    List<AuditLog> findByEntityTypeOrderByTimestampDesc(String entityType);
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);
}