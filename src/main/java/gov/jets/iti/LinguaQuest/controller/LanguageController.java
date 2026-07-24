package gov.jets.iti.LinguaQuest.controller;

import gov.jets.iti.LinguaQuest.dto.request.AddLanguagesRequest;
import gov.jets.iti.LinguaQuest.dto.request.SetActiveLanguageRequest;
import gov.jets.iti.LinguaQuest.dto.response.*;
import gov.jets.iti.LinguaQuest.service.LanguageService;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/{version}/languages")
@RequiredArgsConstructor
public class LanguageController {
    private final LanguageService languageService;

    @GetMapping("/available")
    public ResponseEntity<SuccessResponse<AvailableLanguagesResponse>> getAvailableLanguages(@AuthenticationPrincipal UserPrinciple principle) {
        AvailableLanguagesResponse response = languageService.getAvailableLanguages(principle.user().getId());
        return ResponseEntity.ok(new SuccessResponse<>(true, response));
    }

    @GetMapping("/mine")
    public ResponseEntity<SuccessResponse<MyLanguagesResponse>> getMyLanguages(@AuthenticationPrincipal UserPrinciple principle) {
        MyLanguagesResponse response = languageService.getMyLanguages(principle.user().getId());
        return ResponseEntity.ok(new SuccessResponse<>(true, response));
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<MyLanguagesResponse>> addLanguages(@AuthenticationPrincipal UserPrinciple principle, @RequestBody @Valid AddLanguagesRequest request) {
        MyLanguagesResponse response = languageService.addLanguages(principle.user().getId(), request.languageIds());
        return ResponseEntity.ok(new SuccessResponse<>(true, response));
    }

    @PatchMapping("/active")
    public ResponseEntity<SuccessResponse<ActiveLanguageResponse>> setActiveLanguage(@AuthenticationPrincipal UserPrinciple principle, @RequestBody @Valid SetActiveLanguageRequest request) {
        ActiveLanguageResponse response = languageService.setActiveLanguage(principle.user().getId(), request.languageId());
        return ResponseEntity.ok(new SuccessResponse<>(true, response));
    }

    @PatchMapping("/native")
    public ResponseEntity<SuccessResponse<NativeLanguageDto>> setNativeLanguage(@AuthenticationPrincipal UserPrinciple principle, @RequestBody @Valid SetActiveLanguageRequest request){
        NativeLanguageDto nativeLanguageDto = languageService.setNativeLanguage(principle.user().getId(), request.languageId());
        return ResponseEntity.ok(new SuccessResponse<>(true, nativeLanguageDto));
    }

}
