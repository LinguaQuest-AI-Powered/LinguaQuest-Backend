package gov.jets.iti.LinguaQuest.dto.response;

import gov.jets.iti.LinguaQuest.enums.LevelStatus;

public record LevelDto(Long id, Integer order, LevelStatus status, String word) {
}
