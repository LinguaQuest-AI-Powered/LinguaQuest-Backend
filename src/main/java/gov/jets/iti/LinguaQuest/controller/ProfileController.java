package gov.jets.iti.LinguaQuest.controller;

import gov.jets.iti.LinguaQuest.dto.request.ChangePasswordRequest;
import gov.jets.iti.LinguaQuest.dto.request.CompleteProfileRequest;
import gov.jets.iti.LinguaQuest.dto.request.DeleteProfileRequest;
import gov.jets.iti.LinguaQuest.dto.request.UpdateProfileRequest;
import gov.jets.iti.LinguaQuest.dto.response.OAuthResponseDto;
import gov.jets.iti.LinguaQuest.dto.response.PhotoUploadResponseDto;
import gov.jets.iti.LinguaQuest.dto.response.ProfileResponseDto;
import gov.jets.iti.LinguaQuest.dto.response.ProfileUpdateResponseDto;
import gov.jets.iti.LinguaQuest.dto.response.SuccessResponse;
import gov.jets.iti.LinguaQuest.service.OAuthService;
import gov.jets.iti.LinguaQuest.service.ProfileService;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/{version}/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final OAuthService oAuthService;

    @PostMapping(value = "/complete-profile", version = "v1")
    public ResponseEntity<SuccessResponse<OAuthResponseDto>> completeProfile(
            @AuthenticationPrincipal UserPrinciple principle,
            @RequestBody @Valid CompleteProfileRequest request) {
        OAuthResponseDto response = oAuthService.completeProfile(principle.user().getId(), request);
        return ResponseEntity.ok(new SuccessResponse<>(true, response));
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<ProfileResponseDto>> getProfile(
            @AuthenticationPrincipal UserPrinciple principle) {
        ProfileResponseDto response = profileService.getProfile(principle.user().getId());
        return ResponseEntity.ok(new SuccessResponse<>(true, response));
    }

    @PatchMapping
    public ResponseEntity<SuccessResponse<ProfileUpdateResponseDto>> updateProfile(
            @AuthenticationPrincipal UserPrinciple principle,
            @RequestBody @Valid UpdateProfileRequest request) {
        ProfileUpdateResponseDto response = profileService.updateProfile(principle.user().getId(), request);
        return ResponseEntity.ok(new SuccessResponse<>(true, response));
    }

    @DeleteMapping
    public ResponseEntity<SuccessResponse<Map<String, String>>> deleteProfile(
            @AuthenticationPrincipal UserPrinciple principle,
            @RequestBody(required = false) DeleteProfileRequest request) {
        profileService.deleteProfile(principle.user().getId(), request);
        return ResponseEntity.ok(new SuccessResponse<>(true, Map.of("status", "success")));
    }

    @PostMapping("/photo")
    public ResponseEntity<SuccessResponse<PhotoUploadResponseDto>> updateProfilePhoto(
            @AuthenticationPrincipal UserPrinciple principle,
            @RequestParam("photo") MultipartFile photo) {
        PhotoUploadResponseDto response = profileService.uploadPhoto(principle.user().getId(), photo);
        return ResponseEntity.ok(new SuccessResponse<>(true, response));
    }

    @PatchMapping("/password")
    public ResponseEntity<SuccessResponse<Map<String, String>>> changePassword(
            @AuthenticationPrincipal UserPrinciple principle,
            @RequestBody @Valid ChangePasswordRequest request) {
        profileService.changePassword(principle.user().getId(), request);
        return ResponseEntity.ok(new SuccessResponse<>(true, Map.of("status", "success")));
    }
}
