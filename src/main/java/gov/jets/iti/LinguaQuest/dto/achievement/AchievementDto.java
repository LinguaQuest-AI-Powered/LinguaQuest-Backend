package gov.jets.iti.LinguaQuest.dto.achievement;

import gov.jets.iti.LinguaQuest.enums.AchievementStatus;

import java.time.LocalDateTime;

public record AchievementDto(
        Long id,
        String name,
        String description,
        String iconUrl,
        AchievementStatus status,
        Integer progressPercent,
        Integer targetValue,
        Integer xpReward,
        Integer coinReward,
        LocalDateTime earnedAt
) {}