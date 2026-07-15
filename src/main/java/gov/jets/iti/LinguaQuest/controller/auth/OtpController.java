package gov.jets.iti.LinguaQuest.controller.auth;

import gov.jets.iti.LinguaQuest.dto.request.OtpSendRequest;
import gov.jets.iti.LinguaQuest.dto.request.OtpVerifyRequest;
import gov.jets.iti.LinguaQuest.dto.response.OtpSendResponse;
import gov.jets.iti.LinguaQuest.dto.response.OtpVerifyResponse;
import gov.jets.iti.LinguaQuest.dto.response.SuccessResponse;
import gov.jets.iti.LinguaQuest.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/{version}/auth/otp")
@RequiredArgsConstructor
public class OtpController {

    private final AuthService authService;

    @PostMapping("/send")
    public ResponseEntity<SuccessResponse<OtpSendResponse>> sendOtp(
            @Valid @RequestBody OtpSendRequest request) {

        authService.sendOtp(request);

        return ResponseEntity.ok(
                new SuccessResponse<>(true, new OtpSendResponse("success"))
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<SuccessResponse<OtpVerifyResponse>> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request) {

        authService.verifyOtp(request);

        return ResponseEntity.ok(
                new SuccessResponse<>(true, new OtpVerifyResponse(true))
        );
    }
}