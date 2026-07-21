package gov.jets.iti.LinguaQuest.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompleteProfileRequest(
        @NotNull(message = "nativeLanguageId is required")
        Long nativeLanguageId,

        @NotNull(message = "targetLanguageId is required")
        Long targetLanguageId,

        @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
        String username
) {}
