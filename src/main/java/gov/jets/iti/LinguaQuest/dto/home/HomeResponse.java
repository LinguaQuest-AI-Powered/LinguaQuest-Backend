package gov.jets.iti.LinguaQuest.dto.home;

import gov.jets.iti.LinguaQuest.dto.language.UserLanguageDto;
import gov.jets.iti.LinguaQuest.dto.world.ContinueLevelDto;
import gov.jets.iti.LinguaQuest.dto.world.WorldsResponseDto;

public record HomeResponse(
        Integer xp,
        Integer coins,
        Integer streakDays,
        UserLanguageDto activeLanguage,
        WorldsResponseDto exploreWorlds,
        ContinueLevelDto continueLevel
) {
}
