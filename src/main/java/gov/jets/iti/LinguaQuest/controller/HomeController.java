package gov.jets.iti.LinguaQuest.controller;


import gov.jets.iti.LinguaQuest.dto.response.HomeResponse;
import gov.jets.iti.LinguaQuest.dto.response.SuccessResponse;
import gov.jets.iti.LinguaQuest.service.HomeService;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/{version}/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    ResponseEntity<SuccessResponse<HomeResponse>> getHome(@AuthenticationPrincipal UserPrinciple principle) {
        HomeResponse response = homeService.getHome(principle.user().getId());
        return ResponseEntity.ok(new SuccessResponse<>(true, response));
    }
}
