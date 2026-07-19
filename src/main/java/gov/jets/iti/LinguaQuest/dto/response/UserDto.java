package gov.jets.iti.LinguaQuest.dto.response;

import gov.jets.iti.LinguaQuest.entity.Language;

import java.util.List;
import java.util.Set;

public record UserDto(Long id, String username, String photo,
                      String nativeLanguage, boolean isVerified, Set<Language> targetLanguages) {
}
