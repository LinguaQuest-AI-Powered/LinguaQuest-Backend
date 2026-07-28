package gov.jets.iti.LinguaQuest.dto.auth.response;

public record RefreshTokenResponseDto(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn
) {
}
