package gov.jets.iti.LinguaQuest.exception;

public class DailyRewardAlreadyClaimedException extends RuntimeException {
    public DailyRewardAlreadyClaimedException(String message) {
        super(message);
    }
}
