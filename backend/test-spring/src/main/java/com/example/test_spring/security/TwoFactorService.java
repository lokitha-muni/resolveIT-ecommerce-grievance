package com.example.test_spring.security;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TwoFactorService {

    private final ConcurrentHashMap<String, String> otpStorage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> otpTimestamps = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final SecureRandom random = new SecureRandom();
    private final long OTP_VALIDITY = 5 * 60 * 1000; // 5 minutes

    public TwoFactorService() {
        // Clean expired OTPs every minute
        scheduler.scheduleAtFixedRate(this::cleanExpiredOTPs, 1, 1, TimeUnit.MINUTES);
    }

    public String generateOTP(String email) {
        String otp = String.format("%06d", random.nextInt(1000000));
        otpStorage.put(email, otp);
        otpTimestamps.put(email, System.currentTimeMillis());
        
        // Simulate sending email (in real app, integrate with email service)
        System.out.println("OTP for " + email + ": " + otp);
        
        return otp;
    }

    public boolean verifyOTP(String email, String otp) {
        String storedOTP = otpStorage.get(email);
        Long timestamp = otpTimestamps.get(email);
        
        if (storedOTP == null || timestamp == null) {
            return false;
        }
        
        // Check if OTP is expired
        if (System.currentTimeMillis() - timestamp > OTP_VALIDITY) {
            otpStorage.remove(email);
            otpTimestamps.remove(email);
            return false;
        }
        
        // Verify OTP
        if (storedOTP.equals(otp)) {
            otpStorage.remove(email);
            otpTimestamps.remove(email);
            return true;
        }
        
        return false;
    }

    public boolean hasValidOTP(String email) {
        Long timestamp = otpTimestamps.get(email);
        if (timestamp == null) {
            return false;
        }
        
        return System.currentTimeMillis() - timestamp <= OTP_VALIDITY;
    }

    private void cleanExpiredOTPs() {
        long currentTime = System.currentTimeMillis();
        otpTimestamps.entrySet().removeIf(entry -> {
            if (currentTime - entry.getValue() > OTP_VALIDITY) {
                otpStorage.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    public void invalidateOTP(String email) {
        otpStorage.remove(email);
        otpTimestamps.remove(email);
    }
}