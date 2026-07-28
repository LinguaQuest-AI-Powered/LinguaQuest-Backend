package gov.jets.iti.LinguaQuest.dto.language;

public record UserLanguageDto(
        Long id,
        String name,
        String code,
        String imageUrl,
        Integer level,
        boolean isActive,
        Integer progressPercent
) {
}
