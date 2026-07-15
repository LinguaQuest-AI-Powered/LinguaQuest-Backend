package gov.jets.iti.LinguaQuest.controller.auth;


import gov.jets.iti.LinguaQuest.dto.request.LoginRequestDto;
import gov.jets.iti.LinguaQuest.dto.response.AuthResponseDto;
import gov.jets.iti.LinguaQuest.dto.response.ErrorDetails;
import gov.jets.iti.LinguaQuest.dto.response.ErrorResponse;
import gov.jets.iti.LinguaQuest.dto.response.SuccessResponse;
import gov.jets.iti.LinguaQuest.exception.auth.EmailNotFoundException;
import gov.jets.iti.LinguaQuest.exception.otp.MaxAttemptsExceededException;
import gov.jets.iti.LinguaQuest.service.AuthService;
import gov.jets.iti.LinguaQuest.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/{version}/auth")
@RequiredArgsConstructor
public class AuthController {

    final private AuthService authService;

    @PostMapping(value = "/login",version = "v1")
    public ResponseEntity<SuccessResponse<AuthResponseDto>> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        AuthResponseDto authResponseDto = authService.login(loginRequestDto);
        return ResponseEntity.ok(new SuccessResponse<>(true,authResponseDto));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, "AUTHENTICATION_FAILURE", ex.getMessage(), request);
    }
    @ExceptionHandler({BadCredentialsException.class, EmailNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationExceptions(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.getMessage(), request);
    }
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String errorKey, String message, HttpServletRequest request) {
        ErrorDetails details = new ErrorDetails(
                request.getRequestURI(),
                status.value(),
                errorKey,
                message,
                Instant.now()
        );
        return ResponseEntity.status(status).body(ErrorResponse.of(details));
    }
}
