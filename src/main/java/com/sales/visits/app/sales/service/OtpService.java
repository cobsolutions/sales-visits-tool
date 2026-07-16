package com.sales.visits.app.sales.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class OtpService {
    private static final String OTP_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int OTP_LENGTH = 4;
    private static final Duration OTP_TTL = Duration.ofMinutes(2);
    private static final String KEY_PREFIX = "password-reset-otp:";

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateAndStoreOtp(String email) {
        String otp = generateOtp();
        redisTemplate.opsForValue().set(buildKey(email), otp, OTP_TTL);
        return otp;
    }

    public boolean isValidOtp(String email, String candidateOtp) {
        String stored = redisTemplate.opsForValue().get(buildKey(email));
        return stored != null && stored.equalsIgnoreCase(candidateOtp);
    }

    public void invalidateOtp(String email) {
        redisTemplate.delete(buildKey(email));
    }

    private String generateOtp() {
        StringBuilder sb = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(OTP_CHARS.charAt(secureRandom.nextInt(OTP_CHARS.length())));
        }
        return sb.toString();
    }

    private String buildKey(String email) {
        return KEY_PREFIX + email.toLowerCase();
    }
}
