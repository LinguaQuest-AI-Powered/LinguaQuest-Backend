package gov.jets.iti.LinguaQuest.controller;

import gov.jets.iti.LinguaQuest.dto.common.SuccessResponse;
import gov.jets.iti.LinguaQuest.dto.notification.request.RegisterDeviceRequest;
import gov.jets.iti.LinguaQuest.dto.notification.request.UnregisterDeviceRequest;
import gov.jets.iti.LinguaQuest.dto.notification.response.RegisterDeviceResponse;
import gov.jets.iti.LinguaQuest.repository.DeviceTokenRepository;
import gov.jets.iti.LinguaQuest.service.notification.DeviceTokenService;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "api/{version}/devices")
public class DeviceController {

    private final DeviceTokenService deviceTokenService;

    @PostMapping
    public ResponseEntity<SuccessResponse<RegisterDeviceResponse>> registerDevice(
            @AuthenticationPrincipal UserPrinciple principal,
            @RequestBody RegisterDeviceRequest request) {

        RegisterDeviceResponse response = deviceTokenService.
                registerDevice(principal.user().getId(), request.token(), request.platform());

        return ResponseEntity.ok(new SuccessResponse<>(true, response));
    }

    @DeleteMapping
    public ResponseEntity<SuccessResponse<RegisterDeviceResponse>> unregisterDevice(@RequestBody UnregisterDeviceRequest request) {
        RegisterDeviceResponse response = deviceTokenService.unregisterDevice(request.token());
        return ResponseEntity.ok(new SuccessResponse<>(true,response));
    }
}