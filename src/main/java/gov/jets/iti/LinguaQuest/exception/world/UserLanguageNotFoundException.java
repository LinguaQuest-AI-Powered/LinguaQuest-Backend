package gov.jets.iti.LinguaQuest.exception.world;

public class UserLanguageNotFoundException extends RuntimeException {
    public UserLanguageNotFoundException(String message) {
        super(message);
    }
}