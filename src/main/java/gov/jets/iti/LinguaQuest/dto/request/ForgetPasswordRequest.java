package gov.jets.iti.LinguaQuest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForgetPasswordRequest(
        @NotBlank(message = "resetToken is required")
        String resetToken,

        @NotBlank(message = "newPassword is required")
        @Size(min = 8, message = "newPassword must be at least 8 characters")
        String newPassword
) {}
