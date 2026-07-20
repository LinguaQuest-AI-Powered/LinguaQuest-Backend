package gov.jets.iti.LinguaQuest.dto.request;

import jakarta.validation.constraints.NotNull;

public record SetActiveLanguageRequest(
        @NotNull(message = "languageId is required")
        Long languageId
) {}
