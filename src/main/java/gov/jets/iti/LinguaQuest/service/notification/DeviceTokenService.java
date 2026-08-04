package gov.jets.iti.LinguaQuest.service.notification;


import gov.jets.iti.LinguaQuest.dto.notification.response.RegisterDeviceResponse;
import gov.jets.iti.LinguaQuest.entity.DeviceToken;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.enums.DevicePlatform;
import gov.jets.iti.LinguaQuest.exception.auth.EmailNotFoundException;
import gov.jets.iti.LinguaQuest.repository.DeviceTokenRepository;
import gov.jets.iti.LinguaQuest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {
    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    public RegisterDeviceResponse registerDevice(Long userId, String token, DevicePlatform platform){

        User user = userRepository.findByIdWithNativeLanguage(userId)
                .orElseThrow(() -> new EmailNotFoundException("User not found"));

        DeviceToken deviceToken = deviceTokenRepository.findByToken(token)
                .orElseGet(() -> DeviceToken.builder()
                        .token(token)
                        .build());

        deviceToken.setUser(user);
        deviceToken.setPlatform(platform);
        deviceToken.setLastUsedAt(LocalDateTime.now());
        deviceTokenRepository.save(deviceToken);

        return new RegisterDeviceResponse("success");
    }

    public RegisterDeviceResponse unregisterDevice(String token){
        deviceTokenRepository.deleteByToken(token);
        return new RegisterDeviceResponse("success");
    }
}
