package gov.jets.iti.LinguaQuest.controller;


import gov.jets.iti.LinguaQuest.dto.response.LeaderBoardResponseDto;
import gov.jets.iti.LinguaQuest.dto.response.SuccessResponse;
import gov.jets.iti.LinguaQuest.enums.LeaderboardType;
import gov.jets.iti.LinguaQuest.service.LeaderBoardService;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/{version}/leaderboard")
public class LeaderboardController {

    final private LeaderBoardService leaderBoardService;
    @GetMapping(version = "v1")
    ResponseEntity<SuccessResponse<LeaderBoardResponseDto>> getLeaderBoard(@AuthenticationPrincipal UserPrinciple userPrinciple,
                                                     @RequestParam("scope") LeaderboardType leaderboardType,
                                                     @RequestParam("page") Integer page,
                                                     @RequestParam("limit") Integer limit) {
        LeaderBoardResponseDto leaderBoardResponseDto = leaderBoardService.getLeaderBoard(userPrinciple,leaderboardType,page,limit);
        return ResponseEntity.ok(new SuccessResponse<>(true,leaderBoardResponseDto));
    }
}
