package gov.jets.iti.LinguaQuest.controller.auth;


import gov.jets.iti.LinguaQuest.dto.request.LoginRequestDto;
import gov.jets.iti.LinguaQuest.dto.request.LogoutRequestDto;
import gov.jets.iti.LinguaQuest.dto.request.RegisterRequestDto;
import gov.jets.iti.LinguaQuest.dto.request.RefreshTokenRequestDto;
import gov.jets.iti.LinguaQuest.dto.response.*;
import gov.jets.iti.LinguaQuest.exception.auth.*;
import gov.jets.iti.LinguaQuest.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/{version}/auth")
@RequiredArgsConstructor
public class AuthController {

    final private AuthService authService;

    @PostMapping(value = "/register",version = "v1")
    public ResponseEntity<SuccessResponse<RegisterResponseDto>> register(@RequestBody @Valid RegisterRequestDto registerRequestDto) {
        RegisterResponseDto responseDto = authService.register(registerRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse<>(true,responseDto));
    }

    @PostMapping(value = "/login",version = "v1")
    public ResponseEntity<SuccessResponse<AuthResponseDto>> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        AuthResponseDto authResponseDto = authService.login(loginRequestDto);
        return ResponseEntity.ok(new SuccessResponse<>(true,authResponseDto));
    }

    @PostMapping(value = "/refresh-token", version = "v1")
    public ResponseEntity<SuccessResponse<RefreshTokenResponseDto>> refreshToken(@RequestBody @Valid RefreshTokenRequestDto request) {
        RefreshTokenResponseDto responseDto = authService.refreshToken(request);
        return ResponseEntity.ok(new SuccessResponse<>(true, responseDto));
    }

    @PostMapping(value = "/logout", version = "v1")
    public ResponseEntity<SuccessResponse<LogoutResponseDto>> logout(@RequestBody @Valid LogoutRequestDto logoutRequestDto){
        LogoutResponseDto logoutResponseDto = authService.logout(logoutRequestDto);
        return ResponseEntity.ok(new SuccessResponse<>(true,logoutResponseDto));
    }


    @ExceptionHandler(TargetLanguageNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleTargetLanguageNotSupportedException(TargetLanguageNotSupportedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "LANGUAGE_NOT_SUPPORTED", ex.getMessage(), request);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", ex.getMessage(), request);
    }
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "USERNAME_ALREADY_EXISTS", ex.getMessage(), request);
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILURE", ex.getMessage(), request);
    }
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleEmailNotVerifiedException(EmailNotVerifiedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED", ex.getMessage(), request);
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
