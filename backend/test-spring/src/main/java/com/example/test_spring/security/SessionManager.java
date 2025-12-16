package com.example.test_spring.security;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class SessionManager {

    private final ConcurrentHashMap<String, Long> activeSessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes

    public SessionManager() {
        // Clean expired sessions every 5 minutes
        scheduler.scheduleAtFixedRate(this::cleanExpiredSessions, 5, 5, TimeUnit.MINUTES);
    }

    public void createSession(String token) {
        activeSessions.put(token, System.currentTimeMillis());
    }

    public void updateSession(String token) {
        if (activeSessions.containsKey(token)) {
            activeSessions.put(token, System.currentTimeMillis());
        }
    }

    public boolean isSessionValid(String token) {
        Long lastActivity = activeSessions.get(token);
        if (lastActivity == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastActivity > SESSION_TIMEOUT) {
            activeSessions.remove(token);
            return false;
        }
        
        return true;
    }

    public void invalidateSession(String token) {
        activeSessions.remove(token);
    }

    private void cleanExpiredSessions() {
        long currentTime = System.currentTimeMillis();
        activeSessions.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > SESSION_TIMEOUT
        );
    }

    public int getActiveSessionCount() {
        return activeSessions.size();
    }
}