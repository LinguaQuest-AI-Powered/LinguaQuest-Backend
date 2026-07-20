package gov.jets.iti.LinguaQuest.controller;

import gov.jets.iti.LinguaQuest.dto.response.AvailableLanguagesResponseDto;
import gov.jets.iti.LinguaQuest.dto.response.SuccessResponse;
import gov.jets.iti.LinguaQuest.service.LanguageService;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/{version}/languages")
@RequiredArgsConstructor
public class LanguageController {
    private final LanguageService languageService;

    @GetMapping("/available")
    public ResponseEntity<SuccessResponse<AvailableLanguagesResponseDto>> getAvailableLanguages(@AuthenticationPrincipal UserPrinciple principle) {
        AvailableLanguagesResponseDto response = languageService.getAvailableLanguages(principle.user().getId());
        return ResponseEntity.ok(new SuccessResponse<>(true, response));
    }
}
