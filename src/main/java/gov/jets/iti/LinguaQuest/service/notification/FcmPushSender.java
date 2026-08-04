package gov.jets.iti.LinguaQuest.service.notification;

import com.google.firebase.messaging.*;
import gov.jets.iti.LinguaQuest.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FcmPushSender {

    private final DeviceTokenRepository deviceTokenRepository;

    public void send(String deviceToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            if(e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED){
                deviceTokenRepository.deleteByToken(deviceToken);
            }
            else{
                log.warn("FCM push failed for token {}: {}", deviceToken, e.getMessage());
            }
        }
    }
}