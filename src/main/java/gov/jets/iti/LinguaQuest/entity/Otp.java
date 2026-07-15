package gov.jets.iti.LinguaQuest.entity;

import java.io.Serializable;
import java.time.Instant;

public record Otp(String hashedOtp, int attempts, Instant generatedAt) implements Serializable {}
