package gov.jets.iti.LinguaQuest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FirebaseLoginRequest(
        @NotBlank(message = "idToken is required")
        String idToken
) {}
