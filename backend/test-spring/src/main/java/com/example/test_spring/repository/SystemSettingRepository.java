package com.example.test_spring.repository;

import com.example.test_spring.model.SystemSetting;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemSettingRepository extends MongoRepository<SystemSetting, String> {
    Optional<SystemSetting> findByKey(String key);
    List<SystemSetting> findByCategory(String category);
}