package gov.jets.iti.LinguaQuest.dto.reward;

public record DailyRewardStatusResponse(
        boolean claimedToday,
        Integer currentDay,
        Integer cycleLength,
        Integer rewardCoins,
        Integer rewardXp
) {}