package gov.jets.iti.LinguaQuest.exception.language;

public class LanguageNotFoundException extends RuntimeException {
    public LanguageNotFoundException(String message) {
        super(message);
    }
}