package gov.jets.iti.LinguaQuest.dto.response;

public record DailyRewardClaimResponse(
        Integer coinsAwarded,
        Integer xpAwarded,
        Integer newCoinsBalance,
        Integer newXpBalance,
        Integer nextDay
) {}