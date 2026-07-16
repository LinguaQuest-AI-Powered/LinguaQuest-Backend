package gov.jets.iti.LinguaQuest.exception.auth;

public class InvalidResetTokenException extends RuntimeException {
    public InvalidResetTokenException(String message) {
        super(message);
    }
}