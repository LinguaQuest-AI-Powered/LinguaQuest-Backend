package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.dto.request.OtpSendRequest;
import gov.jets.iti.LinguaQuest.dto.request.OtpVerifyRequest;
import gov.jets.iti.LinguaQuest.entity.OtpPurpose;
import gov.jets.iti.LinguaQuest.exception.auth.EmailNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final OtpService otpService;

    public void sendOtp(OtpSendRequest request) {
        validateEmailExists(request.email());

        otpService.generateAndSendOtp(
                request.email(),
                request.purpose()
        );
    }

    public void verifyOtp(OtpVerifyRequest request) {
        validateEmailExists(request.email());

        otpService.verifyOtp(
                request.email(),
                OtpPurpose.SIGNUP,
                request.otp()
        );

        userService.markEmailVerified(request.email());
    }

    private void validateEmailExists(String email) {
        if (!userService.existsByEmail(email)) {
            throw new EmailNotFoundException("Email not found");
        }
    }
}