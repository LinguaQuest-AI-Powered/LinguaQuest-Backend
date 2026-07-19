package gov.jets.iti.LinguaQuest.dto.response;

import java.util.List;


public record AvailableLanguagesResponseDto(
        List<LanguageOptionDto> languages
) {}