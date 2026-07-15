package gov.jets.iti.LinguaQuest.exception.otp;

public class MaxAttemptsExceededException extends RuntimeException {
    public MaxAttemptsExceededException(String message) {
        super(message);
    }
}
