package gov.jets.iti.LinguaQuest.dto.response;

public record DailyRewardStatusResponse(
        boolean claimedToday,
        Integer currentDay,
        Integer cycleLength,
        Integer rewardCoins,
        Integer rewardXp
) {}