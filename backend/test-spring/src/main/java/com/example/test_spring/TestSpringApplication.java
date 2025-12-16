package com.example.test_spring;

import com.example.test_spring.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.test_spring")
public class TestSpringApplication implements CommandLineRunner {

	@Autowired
	private StaffService staffService;

	public static void main(String[] args) {
		SpringApplication.run(TestSpringApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Initialize default staff and admin accounts on startup
		staffService.initializeDefaultAccounts();
	}
}
