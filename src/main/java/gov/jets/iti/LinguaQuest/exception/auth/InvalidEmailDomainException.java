package gov.jets.iti.LinguaQuest.exception.auth;

public class InvalidEmailDomainException extends RuntimeException {
    public InvalidEmailDomainException(String message) {
        super(message);
    }
}
