package com.example.test_spring.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${mongodb.database:resolveIT_db}")
    private String databaseName;
    
    @Value("${mongodb.username:resolveIT_db_user}")
    private String username;
    
    @Value("${mongodb.password:your_password_here}")
    private String password;
    
    @Value("${mongodb.cluster:cluster0.eqvpfku.mongodb.net}")
    private String cluster;

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @Bean
    @Primary
    @Override
    public MongoClient mongoClient() {
        String connectionString = String.format(
            "mongodb+srv://%s:%s@%s/%s?retryWrites=true&w=majority&appName=Cluster0",
            username, password, cluster, databaseName
        );
        System.out.println("Creating MongoDB client with Atlas connection");
        return MongoClients.create(connectionString);
    }
}