package gov.jets.iti.LinguaQuest.controller.auth;


import gov.jets.iti.LinguaQuest.dto.request.ForgetPasswordRequest;
import gov.jets.iti.LinguaQuest.dto.request.OtpVerifyRequest;
import gov.jets.iti.LinguaQuest.dto.response.ForgetPasswordResponse;
import gov.jets.iti.LinguaQuest.dto.response.PasswordResetOtpVerifyResponse;
import gov.jets.iti.LinguaQuest.dto.response.SuccessResponse;
import gov.jets.iti.LinguaQuest.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/{version}/auth/forget-password")
@RequiredArgsConstructor
public class ForgetPasswordController {

    private final AuthService authService;

    @PostMapping("/otp/verify")
    public ResponseEntity<SuccessResponse<PasswordResetOtpVerifyResponse>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(new SuccessResponse<>(true,authService.verifyPasswordResetOtp(request)));
    }

    @PatchMapping
    public ResponseEntity<SuccessResponse<ForgetPasswordResponse>> resetPassword(@Valid @RequestBody ForgetPasswordRequest request){
        authService.setNewPassword(request);
        return ResponseEntity.ok(new SuccessResponse<>(true, new ForgetPasswordResponse("success")));
    }

}
