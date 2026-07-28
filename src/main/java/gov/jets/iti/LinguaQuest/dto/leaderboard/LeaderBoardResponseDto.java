package gov.jets.iti.LinguaQuest.dto.leaderboard;

import java.util.List;

public record LeaderBoardResponseDto(Integer myRank, List<UserRankDto> topThree, List<UserRankDto> entries) {
}
