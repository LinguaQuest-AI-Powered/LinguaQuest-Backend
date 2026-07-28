package gov.jets.iti.LinguaQuest.dto.world;

public record StartLevelResponse(String targetWord, Integer coins) {
    public StartLevelResponse(String targetWord) {
        this(targetWord, null);
    }
}
