package gov.jets.iti.LinguaQuest.controller;


import gov.jets.iti.LinguaQuest.dto.response.GalleryResponseDto;
import gov.jets.iti.LinguaQuest.dto.response.SuccessResponse;
import gov.jets.iti.LinguaQuest.service.GalleryService;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/{version}/gallery")
public class GalleryController {

    private final GalleryService galleryService;

    @GetMapping(version = "v1")
    public ResponseEntity<SuccessResponse<GalleryResponseDto>> getUserGallery(@AuthenticationPrincipal UserPrinciple userPrinciple) {
        GalleryResponseDto galleryResponseDto = galleryService.getUserGallery(userPrinciple.user());
        return ResponseEntity.ok(new SuccessResponse<>(true, galleryResponseDto));
    }
}
