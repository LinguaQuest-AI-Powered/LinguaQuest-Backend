package gov.jets.iti.LinguaQuest.dto.response;

public record ProfileResponseDto(
        Long id,
        String email,
        String username,
        String nativeLanguage,
        String photoUrl,
        Integer level,
        ProfileStatsDto stats,
        CurrentJourneyDto currentLanguageJourney
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
