package gov.jets.iti.LinguaQuest.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AddLanguagesRequest(
        @NotEmpty(message = "languageIds must contain at least one id")
        List<Long> languageIds
) {}