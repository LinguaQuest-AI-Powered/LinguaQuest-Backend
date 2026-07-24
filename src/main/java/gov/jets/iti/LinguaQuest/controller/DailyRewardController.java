package gov.jets.iti.LinguaQuest.controller;

import gov.jets.iti.LinguaQuest.dto.response.DailyRewardClaimResponse;
import gov.jets.iti.LinguaQuest.dto.response.DailyRewardStatusResponse;
import gov.jets.iti.LinguaQuest.dto.response.SuccessResponse;
import gov.jets.iti.LinguaQuest.service.DailyRewardService;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/{version}/daily-reward")
@RequiredArgsConstructor
public class DailyRewardController {

    private final DailyRewardService dailyRewardService;

    @GetMapping
    public ResponseEntity<SuccessResponse<DailyRewardStatusResponse>> getDailyRewardStatus(@AuthenticationPrincipal UserPrinciple principle){
        DailyRewardStatusResponse response = dailyRewardService.getStatus(principle.user().getId());
        return ResponseEntity.ok(new SuccessResponse<>(true, response));
    }

    @PostMapping("/claim")
    public ResponseEntity<SuccessResponse<DailyRewardClaimResponse>> claim(@AuthenticationPrincipal UserPrinciple principle){
        DailyRewardClaimResponse response = dailyRewardService.claim(principle.user().getId());
        return ResponseEntity.ok(new SuccessResponse<>(true, response));
    }
}
