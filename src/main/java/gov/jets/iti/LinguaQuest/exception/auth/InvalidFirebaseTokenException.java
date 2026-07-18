package gov.jets.iti.LinguaQuest.exception.auth;

public class InvalidFirebaseTokenException extends RuntimeException {
    public InvalidFirebaseTokenException(String message) {
        super(message);
    }
}
