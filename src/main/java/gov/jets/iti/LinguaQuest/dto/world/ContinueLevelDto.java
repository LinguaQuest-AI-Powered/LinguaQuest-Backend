package gov.jets.iti.LinguaQuest.dto.world;

public record ContinueLevelDto(
        Long worldId,
        String worldName,
        Long levelId,
        Integer levelOrder,
        String word
) {}