package gov.jets.iti.LinguaQuest.dto.auth.response;

public record RegisterResponseDto(
        Long id,
        String email,
        String username,
        String nativeLanguage,
        String targetLanguage,
        boolean isVerified
) {
}
