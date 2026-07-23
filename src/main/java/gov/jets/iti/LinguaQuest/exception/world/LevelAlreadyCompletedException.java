package gov.jets.iti.LinguaQuest.exception.world;

public class LevelAlreadyCompletedException extends RuntimeException {
    public LevelAlreadyCompletedException(String message) {
        super(message);
    }
}
