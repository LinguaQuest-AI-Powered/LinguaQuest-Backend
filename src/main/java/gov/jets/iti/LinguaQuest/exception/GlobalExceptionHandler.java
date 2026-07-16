package gov.jets.iti.LinguaQuest.exception;

import gov.jets.iti.LinguaQuest.dto.response.ErrorDetails;
import gov.jets.iti.LinguaQuest.dto.response.ErrorResponse;
import gov.jets.iti.LinguaQuest.exception.auth.EmailNotFoundException;
import gov.jets.iti.LinguaQuest.exception.auth.EmailAlreadyExistsException;
import gov.jets.iti.LinguaQuest.exception.auth.InvalidFirebaseTokenException;
import gov.jets.iti.LinguaQuest.exception.auth.InvalidResetTokenException;
import gov.jets.iti.LinguaQuest.exception.otp.InvalidOtpException;
import gov.jets.iti.LinguaQuest.exception.otp.MaxAttemptsExceededException;
import gov.jets.iti.LinguaQuest.exception.otp.OtpCooldownException;
import io.jsonwebtoken.ExpiredJwtException;
import gov.jets.iti.LinguaQuest.exception.otp.OtpNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmailNotFound(EmailNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "EMAIL_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOtp(InvalidOtpException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_OTP", ex.getMessage(), request);
    }

    @ExceptionHandler(OtpCooldownException.class)
    public ResponseEntity<ErrorResponse> handleOtpCooldown(OtpCooldownException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, "OTP_COOLDOWN", ex.getMessage(), request);
    }

    @ExceptionHandler(MaxAttemptsExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxAttempts(MaxAttemptsExceededException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, "MAX_ATTEMPTS_EXCEEDED", ex.getMessage(), request);
    }

    @ExceptionHandler(OtpNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOtpNotFound(OtpNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "OTP_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Validation failed");
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwt(ExpiredJwtException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "Your session has expired. Please log in again.", request);
    }

    @ExceptionHandler(InvalidFirebaseTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFirebaseToken(InvalidFirebaseTokenException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "INVALID_FIREBASE_TOKEN", ex.getMessage(), request);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred", request);
    }

    @ExceptionHandler(InvalidResetTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidResetToken(InvalidResetTokenException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "INVALID_RESET_TOKEN", ex.getMessage(), request);
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