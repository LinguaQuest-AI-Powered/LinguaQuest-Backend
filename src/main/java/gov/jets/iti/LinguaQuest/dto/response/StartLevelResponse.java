package gov.jets.iti.LinguaQuest.dto.response;

public record StartLevelResponse(String targetWord, Integer coins) {
    public StartLevelResponse(String targetWord) {
        this(targetWord, null);
    }
}
