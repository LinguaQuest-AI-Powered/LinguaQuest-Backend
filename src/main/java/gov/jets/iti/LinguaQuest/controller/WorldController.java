package gov.jets.iti.LinguaQuest.controller;


import gov.jets.iti.LinguaQuest.dto.response.ErrorDetails;
import gov.jets.iti.LinguaQuest.dto.response.ErrorResponse;
import gov.jets.iti.LinguaQuest.dto.response.SuccessResponse;
import gov.jets.iti.LinguaQuest.dto.response.WorldsResponseDto;
import gov.jets.iti.LinguaQuest.enums.Difficulty;
import gov.jets.iti.LinguaQuest.service.WorldService;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/{version}/worlds")
@RequiredArgsConstructor
@Validated
public class WorldController {

    final private WorldService worldService;
    @GetMapping(version = "v1")
    ResponseEntity<SuccessResponse<WorldsResponseDto>> getAllUserWorlds(@AuthenticationPrincipal UserPrinciple userPrinciple,
                                                                        @RequestParam("languageId")
                                                                        Long languageId,
                                                                        @RequestParam("difficulty")
                                                                        @Pattern(regexp = "EASY|MEDIUM|HARD")
                                                                        @NotEmpty(message = "difficulty is required")
                                                                        String difficulty){
        Difficulty diff = Difficulty.valueOf(difficulty);
        WorldsResponseDto worldsResponseDto =  worldService.getAllWorlds(userPrinciple.user().getId(), languageId,diff);
        return ResponseEntity.ok(new SuccessResponse<>(true,worldsResponseDto));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleLanguageNotFound(ConstraintViolationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "difficulty must have a value of this set (EASY - MEDIUM - HARD)", request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String errorKey, String message, HttpServletRequest request) {
        ErrorDetails details = new ErrorDetails(request.getRequestURI(), status.value(), errorKey, message, Instant.now());
        return ResponseEntity.status(status).body(ErrorResponse.of(details));
    }
}
