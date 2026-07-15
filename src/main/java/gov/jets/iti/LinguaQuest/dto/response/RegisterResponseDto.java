package gov.jets.iti.LinguaQuest.dto.response;

public record RegisterResponseDto(
        Long id,
        String email,
        String username,
        String nativeLanguage,
        String targetLanguage,
        boolean isVerified
) {
}
