package gov.jets.iti.LinguaQuest.service.notification;

import gov.jets.iti.LinguaQuest.dto.notification.response.*;
import gov.jets.iti.LinguaQuest.entity.DeviceToken;
import gov.jets.iti.LinguaQuest.entity.Notification;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.enums.NotificationType;
import gov.jets.iti.LinguaQuest.exception.NotificationNotFoundException;
import gov.jets.iti.LinguaQuest.repository.DeviceTokenRepository;
import gov.jets.iti.LinguaQuest.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final FcmPushSender fcmPushSender;
    private final MessageSource messageSource;

    @Transactional
    public void send(User user, NotificationType type, String title, String body) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .body(body)
                .build();
        notificationRepository.save(notification);

        List<DeviceToken> tokens = deviceTokenRepository.findByUserId(user.getId());
        for (DeviceToken token : tokens) {
            fcmPushSender.send(token.getToken(), title, body);
        }
    }

    public NotificationsListDto list(Long userId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<NotificationDto> dtos = page.getContent().stream()
                .map(this::toDto)
                .toList();

        return new NotificationsListDto(
                dtos,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    public UnreadCountResponse unreadCount(Long userId) {
        Long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return new UnreadCountResponse(count);
    }

    @Transactional
    public MarkReadResponse markRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .filter(n -> n.getUser().getId().equals(userId))
                .orElseThrow(() -> new NotificationNotFoundException(
                        "Notification not found for the current user."));

        notification.setIsRead(true);
        notificationRepository.save(notification);
        return new MarkReadResponse("success");
    }

    @Transactional
    public DeleteNotificationResponse delete(Long userId, Long notificationId) {
        int deleted = notificationRepository.deleteByIdAndUserId(notificationId, userId);
        if (deleted == 0) {
            throw new NotificationNotFoundException("Notification not found for the current user.");
        }
        return new DeleteNotificationResponse("success");
    }

    @Transactional
    public DeleteNotificationResponse deleteAll(Long userId) {
        notificationRepository.deleteByUserId(userId);
        return new DeleteNotificationResponse("success");
    }

    @Transactional
    public void broadcastNotification(NotificationType notificationType, String messageKey) {
        List<Notification> notifications = new ArrayList<>();
        Set<User> notifiedUsers = new HashSet<>();
        List<DeviceToken> deviceTokens = deviceTokenRepository.findAll();

        for (DeviceToken token : deviceTokens) {
            User user = token.getUser();
            Locale locale = Locale.forLanguageTag(user.getNativeLanguage().getCode());

            String title = messageSource.getMessage(messageKey + ".title", null, locale);
            String body = messageSource.getMessage(messageKey + ".body", null, locale);

            if (notifiedUsers.add(user)) {
                notifications.add(Notification.builder()
                        .user(user)
                        .type(notificationType)
                        .title(title)
                        .body(body)
                        .build());
            }
            fcmPushSender.send(token.getToken(), title, body);
        }
        notificationRepository.saveAll(notifications);
    }
    private NotificationDto toDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }
}