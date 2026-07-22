package gov.jets.iti.LinguaQuest.controller.auth;

import gov.jets.iti.LinguaQuest.dto.request.CompleteProfileRequest;
import gov.jets.iti.LinguaQuest.dto.request.FirebaseLoginRequest;
import gov.jets.iti.LinguaQuest.dto.response.OAuthResponseDto;
import gov.jets.iti.LinguaQuest.dto.response.SuccessResponse;
import gov.jets.iti.LinguaQuest.service.OAuthService;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/{version}/auth/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;

    @PostMapping(value = "/firebase", version = "v1")
    public ResponseEntity<SuccessResponse<OAuthResponseDto>> firebaseLogin(
            @RequestBody @Valid FirebaseLoginRequest request) {

        OAuthResponseDto response = oAuthService.firebaseLogin(request.idToken());
        return ResponseEntity.ok(new SuccessResponse<>(true, response));
    }

    @PostMapping(value = "/complete-profile", version = "v1")
    public ResponseEntity<SuccessResponse<OAuthResponseDto>> completeProfile(
            @AuthenticationPrincipal UserPrinciple principle,
            @RequestBody @Valid CompleteProfileRequest request) {

        OAuthResponseDto response = oAuthService.completeProfile(principle.user().getId(), request);
        return ResponseEntity.ok(new SuccessResponse<>(true, response));
    }
}
