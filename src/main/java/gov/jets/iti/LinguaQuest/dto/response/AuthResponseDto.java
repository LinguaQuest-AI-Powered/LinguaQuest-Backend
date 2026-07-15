package gov.jets.iti.LinguaQuest.dto.response;

public record AuthResponseDto(String accessToken,String refreshToken,
                              String tokenType,Long expiresIn,
                              UserDto userDto) {
}
