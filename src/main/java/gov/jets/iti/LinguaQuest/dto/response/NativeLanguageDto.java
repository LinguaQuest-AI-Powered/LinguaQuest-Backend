package gov.jets.iti.LinguaQuest.dto.response;

public record NativeLanguageDto(
        Long id,
        String name,
        String code,
        String imageUrl
) {
}
