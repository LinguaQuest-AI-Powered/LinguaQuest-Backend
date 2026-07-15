package gov.jets.iti.LinguaQuest.service;


import gov.jets.iti.LinguaQuest.entity.OtpPurpose;
import gov.jets.iti.LinguaQuest.entity.Otp;
import gov.jets.iti.LinguaQuest.exception.otp.InvalidOtpException;
import gov.jets.iti.LinguaQuest.exception.otp.MaxAttemptsExceededException;
import gov.jets.iti.LinguaQuest.exception.otp.OtpCooldownException;
import gov.jets.iti.LinguaQuest.exception.otp.OtpNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final RedisTemplate<String, Otp> otpRedisTemplate;
    private final MailService mailService;
    private final StringRedisTemplate stringRedisTemplate;

    private static final int OTP_LENGTH = 4;
    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final int MAX_ATTEMPTS = 5;

    public void generateAndSendOtp(String email, OtpPurpose purpose) {
        String cooldownKey = "otp:cooldown:" + purpose + ":" + email;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(cooldownKey))) {
            throw new OtpCooldownException("Please wait before requesting another OTP");
        }

        String otp = generateNumericOtp();
        String hashed = hash(otp);

        String otpKey = "otp:" + purpose + ":" + email;
        otpRedisTemplate.opsForValue().set(otpKey, new Otp(hashed, 0, Instant.now()), OTP_TTL);
        stringRedisTemplate.opsForValue().set(cooldownKey, "1", RESEND_COOLDOWN);
        mailService.sendOtpEmail(email, otp);
    }

    public void verifyOtp(String email, OtpPurpose purpose, String submittedOtp) {
        String otpKey = "otp:" + purpose + ":" + email;
        Otp record = otpRedisTemplate.opsForValue().get(otpKey);

        if (record == null) {
            throw new OtpNotFoundException("OTP expired or not found");
        }
        if (record.attempts() >= MAX_ATTEMPTS) {
            otpRedisTemplate.delete(otpKey);
            throw new MaxAttemptsExceededException("Too many attempts, request a new OTP");
        }

        boolean matches = MessageDigest.isEqual(hash(submittedOtp).getBytes(StandardCharsets.UTF_8), record.hashedOtp().getBytes(StandardCharsets.UTF_8));

        if (matches) {
            otpRedisTemplate.delete(otpKey);
            return;
        }

        Long ttl = otpRedisTemplate.getExpire(otpKey, TimeUnit.SECONDS);
        otpRedisTemplate.opsForValue().set(
                otpKey,
                new Otp(record.hashedOtp(), record.attempts() + 1, record.generatedAt()),
                Duration.ofSeconds(ttl)
        );
        throw new InvalidOtpException("Wrong OTP");
    }

    private String generateNumericOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < OtpService.OTP_LENGTH; i++) sb.append(random.nextInt(10));
        return sb.toString();
    }

    private String hash(String otp) {
        return DigestUtils.sha256Hex(otp);
    }
}
