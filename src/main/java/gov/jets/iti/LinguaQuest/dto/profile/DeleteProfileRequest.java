package gov.jets.iti.LinguaQuest.dto.profile;

import jakarta.validation.constraints.Size;

public record DeleteProfileRequest(
        @Size(max = 64, message = "Password cannot exceed 64 characters")
        String password
) {}

