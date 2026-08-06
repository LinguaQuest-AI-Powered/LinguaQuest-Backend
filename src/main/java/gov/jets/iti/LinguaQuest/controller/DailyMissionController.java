package gov.jets.iti.LinguaQuest.controller;

import gov.jets.iti.LinguaQuest.dto.common.SuccessResponse;
import gov.jets.iti.LinguaQuest.dto.mission.request.DailyMissionRequest;
import gov.jets.iti.LinguaQuest.dto.mission.response.DailyMissionResponse;
import gov.jets.iti.LinguaQuest.dto.mission.response.DailyMissionVerificationResponse;
import gov.jets.iti.LinguaQuest.service.DailyMissionService;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/{version}/missions")
@RequiredArgsConstructor
public class DailyMissionController {

    private final DailyMissionService dailyMissionService;
    @GetMapping(version = "v1")
    public ResponseEntity<SuccessResponse<DailyMissionResponse>> getTodayMission(@AuthenticationPrincipal UserPrinciple userPrinciple) {
        String todayWord = dailyMissionService.getDailyWord(userPrinciple);
        return ResponseEntity.ok(new SuccessResponse<>(true, new DailyMissionResponse(todayWord)));
    }

    @PostMapping(value = "/verify", version = "v1", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse<DailyMissionVerificationResponse>> verifyTodayMission(@Valid DailyMissionRequest dailyMissionRequest,
                                                                                                @AuthenticationPrincipal UserPrinciple userPrinciple) {
        DailyMissionVerificationResponse dailyMissionVerificationResponse = dailyMissionService
                .verifyDailyMission(dailyMissionRequest.image(), dailyMissionRequest.word(), userPrinciple.user());
        return ResponseEntity.ok(new SuccessResponse<>(true, dailyMissionVerificationResponse));
    }

}
