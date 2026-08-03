package gov.jets.iti.LinguaQuest.dto.notification.response;

import java.util.List;

public record NotificationsListDto(
        List<NotificationDto> notifications,
        int page,
        int size,
        long totalElements
) {}