package gov.jets.iti.LinguaQuest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponseDto(String accessToken, String refreshToken,
                              String tokenType, Long expiresIn,
                              @JsonProperty("user") UserDto userDto) {
}
