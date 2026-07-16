package gov.jets.iti.LinguaQuest.controller.auth;

import gov.jets.iti.LinguaQuest.dto.request.FirebaseLoginRequest;
import gov.jets.iti.LinguaQuest.dto.response.OAuthResponseDto;
import gov.jets.iti.LinguaQuest.dto.response.SuccessResponse;
import gov.jets.iti.LinguaQuest.service.OAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}
