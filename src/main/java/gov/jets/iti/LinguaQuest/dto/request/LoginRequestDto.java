package gov.jets.iti.LinguaQuest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(@NotBlank(message = "email is required") @Email(message = "invalid email") String email, @NotBlank(message = "Password is required") String password) {
}
