package gov.jets.iti.LinguaQuest.dto.request;

import gov.jets.iti.LinguaQuest.enums.OtpPurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OtpSendRequest(
        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        String email,

        @NotNull(message = "purpose is required")
        OtpPurpose purpose
) {}