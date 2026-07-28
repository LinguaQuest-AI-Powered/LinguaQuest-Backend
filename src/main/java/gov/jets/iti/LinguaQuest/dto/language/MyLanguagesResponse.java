package gov.jets.iti.LinguaQuest.dto.language;

import java.util.List;

public record MyLanguagesResponse(
        List<UserLanguageDto> languages
) {}