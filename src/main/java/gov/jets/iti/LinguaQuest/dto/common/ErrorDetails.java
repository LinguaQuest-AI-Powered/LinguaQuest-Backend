package gov.jets.iti.LinguaQuest.dto.common;

import java.time.Instant;

public record ErrorDetails(
        String apiPath,
        int errorCode,
        String errorKey,
        String errorMessage,
        Instant errorTime
) {}