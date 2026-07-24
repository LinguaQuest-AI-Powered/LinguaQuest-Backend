package gov.jets.iti.LinguaQuest.dto;

public record UserRankDto(Integer rank, Long userId, String username, String photoUrl, Integer level, Integer xp,
                          Boolean isCurrentUser) {
}
