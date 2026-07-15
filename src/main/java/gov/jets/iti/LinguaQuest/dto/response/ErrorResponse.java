package gov.jets.iti.LinguaQuest.dto.response;

public record ErrorResponse(boolean success, ErrorDetails error) {
    public static ErrorResponse of(ErrorDetails error) {
        return new ErrorResponse(false, error);
    }
}
