package gov.jets.iti.LinguaQuest.dto.auth.request;

import gov.jets.iti.LinguaQuest.enums.OtpPurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OtpSendRequest(
        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        @Size(max = 254, message = "Email cannot exceed 254 characters")
        String email,

        @NotNull(message = "purpose is required")
        OtpPurpose purpose
) {}