package gov.jets.iti.LinguaQuest.dto.profile;

import gov.jets.iti.LinguaQuest.dto.achievement.AchievementDto;
import gov.jets.iti.LinguaQuest.dto.leaderboard.UserRankDto;

import java.util.List;

public record ProfileResponseDto(
        Long id,
        String email,
        String username,
        String nativeLanguage,
        String photoUrl,
        Integer level,
        ProfileStatsDto stats,
        CurrentJourneyDto currentLanguageJourney,
        List<AchievementDto> achievements,
        List<UserRankDto> leaderboard
) {
    public record ProfileStatsDto(
            Integer coins,
            Integer totalXp,
            Integer streakDays,
            Integer worldsCount
    ) {}

    public record CurrentJourneyDto(
            Long languageId,
            String name,
            String code,
            Integer level,
            String journeyLabel,
            Integer currentXp,
            Integer nextMilestoneXp
    ) {}
}
