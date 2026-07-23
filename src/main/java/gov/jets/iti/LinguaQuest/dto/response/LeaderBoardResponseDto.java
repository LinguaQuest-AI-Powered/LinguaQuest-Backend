package gov.jets.iti.LinguaQuest.dto.response;

import gov.jets.iti.LinguaQuest.dto.UserRankDto;

import java.util.List;

public record LeaderBoardResponseDto(Integer myRank, List<UserRankDto> topThree, List<UserRankDto> entries) {
}
