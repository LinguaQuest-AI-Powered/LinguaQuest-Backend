package gov.jets.iti.LinguaQuest.exception.auth;

public class TargetLanguageNotSupportedException extends RuntimeException {
    public TargetLanguageNotSupportedException(String message) {
        super(message);
    }
}
