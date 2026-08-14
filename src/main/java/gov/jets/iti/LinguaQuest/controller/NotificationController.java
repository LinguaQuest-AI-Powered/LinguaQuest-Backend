package gov.jets.iti.LinguaQuest.controller;

import gov.jets.iti.LinguaQuest.dto.common.SuccessResponse;
import gov.jets.iti.LinguaQuest.dto.notification.response.DeleteNotificationResponse;
import gov.jets.iti.LinguaQuest.dto.notification.response.MarkReadResponse;
import gov.jets.iti.LinguaQuest.dto.notification.response.NotificationsListDto;
import gov.jets.iti.LinguaQuest.dto.notification.response.UnreadCountResponse;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.enums.NotificationType;
import gov.jets.iti.LinguaQuest.service.notification.NotificationService;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "api/{version}/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final MessageSource messageSource;

    @GetMapping
    public ResponseEntity<SuccessResponse<NotificationsListDto>> list(
            @AuthenticationPrincipal UserPrinciple principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        NotificationsListDto response = notificationService.list(principal.user().getId(), pageable);
        return ResponseEntity.ok(new SuccessResponse<>(true, response));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<SuccessResponse<UnreadCountResponse>> unreadCount(@AuthenticationPrincipal UserPrinciple principal) {
        UnreadCountResponse response = notificationService.unreadCount(principal.user().getId());
        return ResponseEntity.ok(new SuccessResponse<>(true, response));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<SuccessResponse<MarkReadResponse>> markRead(
            @AuthenticationPrincipal UserPrinciple principal,
            @PathVariable Long id) {

        MarkReadResponse response = notificationService.markRead(principal.user().getId(), id);
        return ResponseEntity.ok(new SuccessResponse<>(true, response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<DeleteNotificationResponse>> deleteNotification(
            @AuthenticationPrincipal UserPrinciple principal,
            @PathVariable Long id) {

        DeleteNotificationResponse response = notificationService.delete(principal.user().getId(), id);
        return ResponseEntity.ok(new SuccessResponse<>(true, response));
    }

    @DeleteMapping
    public ResponseEntity<SuccessResponse<DeleteNotificationResponse>> deleteAllNotifications(@AuthenticationPrincipal UserPrinciple principal) {
        DeleteNotificationResponse response = notificationService.deleteAll(principal.user().getId());
        return ResponseEntity.ok(new SuccessResponse<>(true, response));
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<String>> sendTestNotification(
            @AuthenticationPrincipal UserPrinciple principal,
            @RequestParam(defaultValue = "SYSTEM") String type,
            @RequestParam(defaultValue = "test.notification") String messageKey) {

        NotificationType notificationType;
        try {
            notificationType = NotificationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid notification type: " + type);
        }

        User user = principal.user();
        Locale locale = Locale.forLanguageTag(user.getNativeLanguage().getCode());

        String title = messageSource.getMessage(messageKey + ".title", null, locale);
        String body = messageSource.getMessage(messageKey + ".body", null, locale);

        notificationService.send(user, notificationType, title, body);
        return ResponseEntity.ok(new SuccessResponse<>(true, "Test notification sent to user " + user.getId()));
    }
}