package gov.jets.iti.LinguaQuest.controller;

import gov.jets.iti.LinguaQuest.dto.achievement.AchievementsListData;
import gov.jets.iti.LinguaQuest.dto.common.SuccessResponse;
import gov.jets.iti.LinguaQuest.service.achievement.AchievementQueryService;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "api/{version}/achievements")
public class AchievementController {

    private final AchievementQueryService achievementQueryService;

    @GetMapping
    public ResponseEntity<SuccessResponse<AchievementsListData>> listAchievements(
            @AuthenticationPrincipal UserPrinciple principal,
            @RequestParam(defaultValue = "ALL") String status) {

        AchievementsListData data = achievementQueryService.getAchievements(principal.user().getId(), status);
        return ResponseEntity.ok(new SuccessResponse<>(true,data));
    }
}