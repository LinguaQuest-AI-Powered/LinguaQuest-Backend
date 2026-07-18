package gov.jets.iti.LinguaQuest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDto(@NotBlank(message = "email cannot be blank") @Email(message = "Invalid email format") String email,
                                 @NotBlank(message = "username cannot be blank") String username,
                                 @NotBlank(message = "password cannot be blank") String password,
                                 @NotBlank(message = "User must have a native language") String nativeLanguage,
                                 @NotBlank(message = "targetLanguage cannot be blank") String targetLanguage) {

}
