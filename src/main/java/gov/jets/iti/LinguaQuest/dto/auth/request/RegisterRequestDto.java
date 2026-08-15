package gov.jets.iti.LinguaQuest.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(@NotBlank(message = "email cannot be blank") @Email(message = "Invalid email format") @Size(max = 254, message = "Email cannot exceed 254 characters") String email,
                                 @NotBlank(message = "username cannot be blank") @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters") @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores") String username,
                                 @NotBlank(message = "password cannot be blank") @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters") String password,
                                 @NotBlank(message = "User must have a native language") String nativeLanguage,
                                 @NotBlank(message = "targetLanguage cannot be blank") String targetLanguage) {

}

