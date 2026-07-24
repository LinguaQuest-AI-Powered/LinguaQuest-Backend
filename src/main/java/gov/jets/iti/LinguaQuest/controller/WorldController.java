package gov.jets.iti.LinguaQuest.controller;


import gov.jets.iti.LinguaQuest.dto.response.*;
import gov.jets.iti.LinguaQuest.enums.Difficulty;
import gov.jets.iti.LinguaQuest.service.GameService;
import gov.jets.iti.LinguaQuest.service.WorldService;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/{version}/worlds")
@RequiredArgsConstructor
@Validated
public class WorldController {

    final private WorldService worldService;
    final private GameService gameService;

    @GetMapping(version = "v1")
    ResponseEntity<SuccessResponse<WorldsResponseDto>> getAllUserWorlds(@AuthenticationPrincipal UserPrinciple userPrinciple,
                                                                        @RequestParam("difficulty")
                                                                        @Pattern(regexp = "EASY|MEDIUM|HARD|ALL")
                                                                        @NotEmpty(message = "difficulty is required")
                                                                        String difficulty){
        Difficulty diff = Difficulty.valueOf(difficulty);
        WorldsResponseDto worldsResponseDto = worldService.getAllWorlds(userPrinciple.user().getId(),diff);
        return ResponseEntity.ok(new SuccessResponse<>(true,worldsResponseDto));
    }

    @GetMapping(value = "/{worldId}/levels" , version = "v1")
    ResponseEntity<SuccessResponse<WorldLevelsResponseDto>> getLevelsOfAWorld(@AuthenticationPrincipal UserPrinciple userPrinciple ,
                                                                              @PathVariable("worldId") Long worldId) {

        WorldLevelsResponseDto worldsResponseDto = worldService.getWorldLevels(userPrinciple.user().getId(), worldId);
        return ResponseEntity.ok(new SuccessResponse<>(true,worldsResponseDto));
    }

    @PostMapping(value = "/{worldId}/levels/{levelId}/start", version = "v1")
    public ResponseEntity<SuccessResponse<StartLevelResponse>> startLevel(
            @PathVariable Long worldId,
            @PathVariable Long levelId,
            @AuthenticationPrincipal UserPrinciple principle) {

        StartLevelResponse data = gameService.startLevel(
                principle.user().getId(), worldId, levelId);

        return ResponseEntity.ok(new SuccessResponse<>(true, data));
    }

    @PutMapping(value = "/{worldId}/levels/{levelId}/change-word", version = "v1")
    public ResponseEntity<SuccessResponse<StartLevelResponse>> changeWord(
            @PathVariable Long worldId,
            @PathVariable Long levelId,
            @AuthenticationPrincipal UserPrinciple principle) {

        StartLevelResponse data = gameService.changeWord(
                principle.user().getId(), worldId, levelId);

        return ResponseEntity.ok(new SuccessResponse<>(true, data));
    }

    @PostMapping(value = "/{worldId}/levels/{levelId}/verify" , version = "v1",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse<VerifyImageResponse>> verifyImage(
            @PathVariable Long worldId,
            @PathVariable Long levelId,
            @RequestParam("image") MultipartFile image,
            @AuthenticationPrincipal UserPrinciple principle) {

        VerifyImageResponse data = gameService.verifyImage(
                principle.user().getId(), worldId, levelId, image);

        return ResponseEntity.ok(new SuccessResponse<>(true, data));
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleLanguageNotFound(ConstraintViolationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "difficulty must have a value of this set (EASY - MEDIUM - HARD - ALL)", request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String errorKey, String message, HttpServletRequest request) {
        ErrorDetails details = new ErrorDetails(request.getRequestURI(), status.value(), errorKey, message, Instant.now());
        return ResponseEntity.status(status).body(ErrorResponse.of(details));
    }

}
