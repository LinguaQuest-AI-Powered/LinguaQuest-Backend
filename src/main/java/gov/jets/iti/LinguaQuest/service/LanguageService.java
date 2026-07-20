package gov.jets.iti.LinguaQuest.service;


import gov.jets.iti.LinguaQuest.dto.response.AvailableLanguagesResponseDto;
import gov.jets.iti.LinguaQuest.dto.response.LanguageOptionDto;
import gov.jets.iti.LinguaQuest.entity.Language;
import gov.jets.iti.LinguaQuest.repository.LanguageRepository;
import gov.jets.iti.LinguaQuest.repository.UserLanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LanguageService {
    private final LanguageRepository languageRepository;
    private final UserLanguageRepository userLanguageRepository;

    public AvailableLanguagesResponseDto getAvailableLanguages(Long userId) {
        List<Language> languagesList = languageRepository.findAllByOrderByNameAsc();
        Set<Long> addedLanguagesIds = userLanguageRepository.findLanguageIdsByUserId(userId);

        List<LanguageOptionDto> languages = languagesList.stream()
                .map(language -> new LanguageOptionDto(
                        language.getId(),
                        language.getName(),
                        language.getCode(),
                        language.getImageUrl(),
                        addedLanguagesIds.contains(language.getId())
                ))
                .toList();

        return new AvailableLanguagesResponseDto(languages);
    }
}
