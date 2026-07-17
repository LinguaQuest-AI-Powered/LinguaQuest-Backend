package gov.jets.iti.LinguaQuest.dto.response;

public record PasswordResetOtpVerifyResponse(String resetToken, long expiresIn) {}