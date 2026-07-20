package gov.jets.iti.LinguaQuest.dto.response;

import gov.jets.iti.LinguaQuest.enums.Difficulty;

import java.util.List;

public record WorldLevelsResponseDto(Long id, String name, Difficulty difficulty, List<LevelDto> levels) {
}
