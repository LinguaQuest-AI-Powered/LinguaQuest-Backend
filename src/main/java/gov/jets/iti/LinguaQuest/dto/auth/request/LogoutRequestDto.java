package gov.jets.iti.LinguaQuest.dto.auth.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequestDto(@NotBlank(message = "refreshToken must be populated") String refreshToken,
                               Boolean allDevices) {
    public LogoutRequestDto {
        if (allDevices == null) {
            allDevices = false;
        }
    }
}
