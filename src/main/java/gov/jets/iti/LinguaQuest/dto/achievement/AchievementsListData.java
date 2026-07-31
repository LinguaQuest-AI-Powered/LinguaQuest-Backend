package gov.jets.iti.LinguaQuest.dto.achievement;

import java.util.List;

public record AchievementsListData(
        Integer earnedCount,
        Integer inProgressCount,
        Integer xpEarned,
        List<AchievementDto> achievements
) {}