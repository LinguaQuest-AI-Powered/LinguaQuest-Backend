package gov.jets.iti.LinguaQuest.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.NotBlank;

public record LogoutRequestDto(@NotBlank(message = "refreshToken must be populated") String refreshToken,
                               Boolean allDevices) {
    public LogoutRequestDto {
        if (allDevices == null) {
            allDevices = false;
        }
    }
}
