package gov.jets.iti.LinguaQuest.dto.response;

import gov.jets.iti.LinguaQuest.enums.Difficulty;

public record WorldDto(Long id, String name, String imageUrl, Difficulty difficulty,
                       Long progressPercent,Long totalLevels,Long completedLevels) {
}
