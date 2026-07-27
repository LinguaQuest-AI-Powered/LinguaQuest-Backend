package gov.jets.iti.LinguaQuest.dto;

import gov.jets.iti.LinguaQuest.enums.Difficulty;

import java.time.LocalDateTime;

public interface SolvedWordDto {
    String getWord();
    String getNativeWord();
    Long getWorldId();
    String getWorldName();
    String getWorldImageUrl();
}
