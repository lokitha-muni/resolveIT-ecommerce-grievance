package com.example.test_spring.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "resolveIT_db";
    }

    @Bean
    @Primary
    @Override
    public MongoClient mongoClient() {
        String connectionString = "mongodb+srv://resolveIT_db_user:lokitha2005@cluster0.eqvpfku.mongodb.net/resolveIT_db?retryWrites=true&w=majority&appName=Cluster0";
        System.out.println("Creating MongoDB client with Atlas connection: " + connectionString);
        return MongoClients.create(connectionString);
    }
}