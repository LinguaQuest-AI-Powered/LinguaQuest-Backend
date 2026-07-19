package gov.jets.iti.LinguaQuest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequestDto(@NotBlank(message = "refreshToken must be populated") String refreshToken,
                               boolean allDevices) {
}
