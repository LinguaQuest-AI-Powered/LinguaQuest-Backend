package gov.jets.iti.LinguaQuest.dto.response;

public record OAuthResponseDto(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        boolean profileComplete,
        UserDto user
) {}
