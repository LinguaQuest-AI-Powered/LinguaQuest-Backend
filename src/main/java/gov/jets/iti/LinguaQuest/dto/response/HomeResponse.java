package gov.jets.iti.LinguaQuest.dto.response;

public record HomeResponse(
        Integer xp,
        Integer coins,
        Integer streakDays,
        UserLanguageDto activeLanguage,
        WorldsResponseDto exploreWorlds
) {
}
