package gov.jets.iti.LinguaQuest.dto.common;

public record ErrorResponse(boolean success, ErrorDetails error) {
    public static ErrorResponse of(ErrorDetails error) {
        return new ErrorResponse(false, error);
    }
}
