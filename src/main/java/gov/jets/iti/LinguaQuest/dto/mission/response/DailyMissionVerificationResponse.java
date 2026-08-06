package gov.jets.iti.LinguaQuest.dto.mission.response;

public record DailyMissionVerificationResponse(boolean isMatch, Integer xpEarned, Integer coinsEarned) {
}
