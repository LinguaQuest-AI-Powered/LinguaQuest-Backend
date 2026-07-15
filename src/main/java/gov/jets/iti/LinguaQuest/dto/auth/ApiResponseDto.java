package gov.jets.iti.LinguaQuest.dto.auth;

public record ApiResponseDto<T>(boolean success,T data) {
}
