package gov.jets.iti.LinguaQuest.dto.language;

import jakarta.validation.constraints.NotNull;

public record SetActiveLanguageRequest(
        @NotNull(message = "languageId is required")
        Long languageId
) {}
