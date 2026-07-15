package gov.jets.iti.LinguaQuest.exception.otp;

public class OtpCooldownException extends RuntimeException {
    public OtpCooldownException(String message) {
        super(message);
    }
}
