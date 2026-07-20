package gov.jets.iti.LinguaQuest.dto.response;

import java.util.List;

public record MyLanguagesResponseDto(
        List<UserLanguageDto> languages
) {}