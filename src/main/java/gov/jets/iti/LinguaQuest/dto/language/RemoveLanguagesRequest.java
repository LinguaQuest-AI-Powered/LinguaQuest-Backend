package gov.jets.iti.LinguaQuest.dto.language;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RemoveLanguagesRequest(
        @NotEmpty(message = "languageIds must contain at least one id")
        List<Long> languageIds
) {}
