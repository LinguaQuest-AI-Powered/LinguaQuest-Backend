package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.dto.response.LeaderBoardResponseDto;
import gov.jets.iti.LinguaQuest.enums.LeaderboardType;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;

@Service
public class LeaderBoardService {

    public LeaderBoardResponseDto getLeaderBoard(UserPrinciple userPrinciple, LeaderboardType leaderboardType, Integer page, Integer limit) {

        return new LeaderBoardResponseDto(200,new ArrayList<>(),new ArrayList<>());
    }
}
