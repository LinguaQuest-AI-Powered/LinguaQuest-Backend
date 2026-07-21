package gov.jets.iti.LinguaQuest.exception;

import gov.jets.iti.LinguaQuest.dto.response.ErrorDetails;
import gov.jets.iti.LinguaQuest.dto.response.ErrorResponse;
import gov.jets.iti.LinguaQuest.exception.auth.EmailNotFoundException;
import gov.jets.iti.LinguaQuest.exception.auth.EmailAlreadyExistsException;
import gov.jets.iti.LinguaQuest.exception.auth.InvalidFirebaseTokenException;
import gov.jets.iti.LinguaQuest.exception.auth.InvalidResetTokenException;
import gov.jets.iti.LinguaQuest.exception.auth.RefreshTokenExpiredException;
import gov.jets.iti.LinguaQuest.exception.auth.InvalidRefreshTokenException;
import gov.jets.iti.LinguaQuest.exception.language.InvalidLanguageIdException;
import gov.jets.iti.LinguaQuest.exception.language.LanguageAlreadyAddedException;
import gov.jets.iti.LinguaQuest.exception.language.LanguageNotFoundException;
import gov.jets.iti.LinguaQuest.exception.otp.InvalidOtpException;
import gov.jets.iti.LinguaQuest.exception.otp.MaxAttemptsExceededException;
import gov.jets.iti.LinguaQuest.exception.otp.OtpCooldownException;
import gov.jets.iti.LinguaQuest.exception.world.WorldNotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import gov.jets.iti.LinguaQuest.exception.otp.OtpNotFoundException;
import gov.jets.iti.LinguaQuest.exception.profile.InvalidPasswordException;
import gov.jets.iti.LinguaQuest.exception.profile.UsernameAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.web.bind.MissingServletRequestParameterException;

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
        String message = ex.getBindingResult().getFieldErrors().stream().findFirst().map(FieldError::getDefaultMessage).orElse("Validation failed");
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

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenExpired(RefreshTokenExpiredException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(InvalidRefreshTokenException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", ex.getMessage(), request);
    }

    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<ErrorResponse> handleImageUpload(ImageUploadException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "IMAGE_UPLOAD_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(org.springframework.web.multipart.MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONTENT_TOO_LARGE, "MAX_FILE_SIZE_EXCEEDED", "Uploaded file exceeds the maximum allowed limit of 10MB", request);
    }

    @ExceptionHandler(LanguageAlreadyAddedException.class)
    public ResponseEntity<ErrorResponse> handleLanguageAlreadyAdded(LanguageAlreadyAddedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "LANGUAGE_ALREADY_ADDED", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidLanguageIdException.class)
    public ResponseEntity<ErrorResponse> handleInvalidLanguageId(InvalidLanguageIdException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(LanguageNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLanguageNotFound(LanguageNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "LANGUAGE_NOT_FOUND", ex.getMessage(), request);
    }


    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        String message = String.format(
                "%s query parameter is required",
                ex.getParameterName()
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                message,
                request
        );
    }

    @ExceptionHandler(WorldNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWorldNotFoundException(WorldNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "WORLD_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "USERNAME_ALREADY_EXISTS", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPassword(InvalidPasswordException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD", ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String errorKey, String message, HttpServletRequest request) {
        ErrorDetails details = new ErrorDetails(request.getRequestURI(), status.value(), errorKey, message, Instant.now());
        return ResponseEntity.status(status).body(ErrorResponse.of(details));
    }
}