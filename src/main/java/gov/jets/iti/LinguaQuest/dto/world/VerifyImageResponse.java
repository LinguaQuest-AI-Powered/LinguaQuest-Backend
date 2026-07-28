package gov.jets.iti.LinguaQuest.dto.world;

public record VerifyImageResponse(
        boolean isMatch,
        int xpEarned,
        int coinsEarned,
        int level,
        int levelProgressPercentage
) {}
