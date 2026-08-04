package gov.jets.iti.LinguaQuest.dto.notification.response;
import gov.jets.iti.LinguaQuest.enums.NotificationType;
import java.time.LocalDateTime;
public record NotificationDto(
        Long id,
        NotificationType type,
        String title,
        String body,
        Boolean isRead,
        LocalDateTime createdAt
) {}