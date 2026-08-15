package gov.jets.iti.LinguaQuest.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
        @NotBlank(message = "email is required")
        @Email(message = "invalid email")
        @Size(max = 254, message = "Email cannot exceed 254 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(max = 64, message = "Password cannot exceed 64 characters")
        String password
) {}

