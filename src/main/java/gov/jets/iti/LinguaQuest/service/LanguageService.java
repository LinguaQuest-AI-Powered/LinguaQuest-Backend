package gov.jets.iti.LinguaQuest.service;


import gov.jets.iti.LinguaQuest.dto.response.*;
import gov.jets.iti.LinguaQuest.entity.Language;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.entity.UserLanguage;
import gov.jets.iti.LinguaQuest.exception.language.InvalidLanguageIdException;
import gov.jets.iti.LinguaQuest.exception.language.LanguageAlreadyAddedException;
import gov.jets.iti.LinguaQuest.exception.language.LanguageNotFoundException;
import gov.jets.iti.LinguaQuest.repository.LanguageRepository;
import gov.jets.iti.LinguaQuest.repository.UserLanguageRepository;
import gov.jets.iti.LinguaQuest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LanguageService {
    private final LanguageRepository languageRepository;
    private final UserLanguageRepository userLanguageRepository;
    private final UserRepository userRepository;

    public AvailableLanguagesResponse getAvailableLanguages(Long userId) {
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

        return new AvailableLanguagesResponse(languages);
    }
    public MyLanguagesResponse getMyLanguages(Long userId){
        List<UserLanguage> userLanguagesList = userLanguageRepository.findAllByUserIdWithLanguage(userId);
        List<UserLanguageDto> languages = userLanguagesList.stream()
                .map(userLanguage -> new UserLanguageDto(
                            userLanguage.getLanguage().getId(),
                            userLanguage.getLanguage().getName(),
                            userLanguage.getLanguage().getCode(),
                            userLanguage.getLanguage().getImageUrl(),
                            userLanguage.getLevel(),
                            userLanguage.isActive(),
                            userLanguage.getProgressPercent()
                ))
                .toList();

        return new MyLanguagesResponse(languages);
    }

    @Transactional
    public MyLanguagesResponse addLanguages(Long userId, List<Long> languageIds){
        Set<Long> requestedIds = new LinkedHashSet<>(languageIds);
        List<Language> languages = languageRepository.findAllById(requestedIds);
        if (languages.size() != requestedIds.size()) {
            throw new InvalidLanguageIdException("One or more selected languages do not exist");
        }
        Set<Long> alreadyAddedIds = userLanguageRepository.findLanguageIdsByUserId(userId);
        boolean anyConflict = requestedIds.stream().anyMatch(alreadyAddedIds::contains);
        if (anyConflict) {
            throw new LanguageAlreadyAddedException(
                    "One or more selected languages are already in your profile");
        }
        User userRef = userRepository.getReferenceById(userId);

        List<UserLanguage> newRows = languages.stream()
                .map(language -> UserLanguage.builder()
                        .user(userRef)
                        .language(language)
                        .build())
                .toList();

        userLanguageRepository.saveAll(newRows);

        return getMyLanguages(userId);
    }

    @Transactional
    public ActiveLanguageResponse setActiveLanguage(Long userId, Long languageId){
        UserLanguage userLanguage = userLanguageRepository
                .findByUserIdAndLanguageIdWithLanguage(userId,languageId)
                .orElseThrow(()-> new LanguageNotFoundException("This language is not in your profile yet"));
        userLanguage.setActive(true);
        userLanguageRepository.deactivateAllExcept(userId,userLanguage.getId());
        userLanguageRepository.save(userLanguage);

        UserLanguageDto userLanguageDto = new UserLanguageDto(
                userLanguage.getLanguage().getId(),
                userLanguage.getLanguage().getName(),
                userLanguage.getLanguage().getCode(),
                userLanguage.getLanguage().getImageUrl(),
                userLanguage.getLevel(),
                userLanguage.isActive(),
                userLanguage.getProgressPercent()
        );
        return new ActiveLanguageResponse(userLanguageDto);
    }

}
