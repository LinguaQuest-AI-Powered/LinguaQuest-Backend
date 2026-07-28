package gov.jets.iti.LinguaQuest.dto.language;


public record LanguageOptionDto(
        Long id,
        String name,
        String code,
        String imageUrl,
        boolean isAdded
) {}