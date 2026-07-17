package gov.jets.iti.LinguaQuest.dto.response;

public record RefreshTokenResponseDto(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn
) {
}
