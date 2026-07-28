package gov.jets.iti.LinguaQuest.dto.auth.response;

public record PasswordResetOtpVerifyResponse(String resetToken, long expiresIn) {}