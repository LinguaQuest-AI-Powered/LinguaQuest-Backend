package gov.jets.iti.LinguaQuest.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Old password is required")
        @Size(max = 64, message = "Old password must not exceed 64 characters")
        String oldPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 64, message = "New password must be between 8 and 64 characters")
        String newPassword
) {}
