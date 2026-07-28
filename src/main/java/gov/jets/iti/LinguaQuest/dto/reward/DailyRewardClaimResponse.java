package gov.jets.iti.LinguaQuest.dto.reward;

public record DailyRewardClaimResponse(
        Integer coinsAwarded,
        Integer xpAwarded,
        Integer newCoinsBalance,
        Integer newXpBalance,
        Integer nextDay
) {}