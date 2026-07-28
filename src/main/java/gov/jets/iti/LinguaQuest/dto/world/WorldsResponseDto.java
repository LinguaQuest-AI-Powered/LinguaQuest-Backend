package gov.jets.iti.LinguaQuest.dto.world;

import java.util.List;

public record WorldsResponseDto(Integer totalCount, List<WorldDto> worlds) {
}
