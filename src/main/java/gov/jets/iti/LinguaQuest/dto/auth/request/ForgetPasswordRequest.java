package gov.jets.iti.LinguaQuest.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForgetPasswordRequest(
        @NotBlank(message = "resetToken is required")
        String resetToken,

        @NotBlank(message = "newPassword is required")
        @Size(min = 8, max = 64, message = "New password must be between 8 and 64 characters")
        String newPassword
) {}
