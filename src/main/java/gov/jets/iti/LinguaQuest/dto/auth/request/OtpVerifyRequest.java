package gov.jets.iti.LinguaQuest.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OtpVerifyRequest(
        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        @Size(max = 254, message = "Email cannot exceed 254 characters")
        String email,

        @NotBlank(message = "otp is required")
        @Size(min = 4, max = 4, message = "OTP must be 4 characters")
        @Pattern(regexp = "^\\d{4}$", message = "OTP must be a 4-digit number")
        String otp
) {}