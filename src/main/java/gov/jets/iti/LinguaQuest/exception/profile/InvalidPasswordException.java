package gov.jets.iti.LinguaQuest.exception.profile;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
