package gov.jets.iti.LinguaQuest.dto.response;

public record VerifyImageResponse(
        boolean isMatch,
        int xpEarned,
        int coinsEarned,
        int level,
        int levelProgressPercentage
) {}
