package com.example.test_spring.repository;

import com.example.test_spring.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserEmailOrderByCreatedAtDesc(String userEmail);
    List<Notification> findByUserEmailAndRead(String userEmail, boolean read);
    long countByUserEmailAndIsRead(String userEmail, boolean isRead);
    long countByUserEmailAndRead(String userEmail, boolean read);
}