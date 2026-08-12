package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.dto.world.SolvedWordDto;
import gov.jets.iti.LinguaQuest.dto.world.CustomWorldDto;
import gov.jets.iti.LinguaQuest.dto.gallery.GalleryResponseDto;
import gov.jets.iti.LinguaQuest.dto.world.WordDto;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.entity.UserLanguage;
import gov.jets.iti.LinguaQuest.enums.TranslatableEntityType;
import gov.jets.iti.LinguaQuest.exception.language.NoActiveLanguageException;
import gov.jets.iti.LinguaQuest.repository.UserLanguageRepository;
import gov.jets.iti.LinguaQuest.repository.UserLevelProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GalleryService {

    final private UserLevelProgressRepository userLevelProgressRepository;
    final private UserLanguageRepository userLanguageRepository;
    final private TranslationResolverService translationResolver;

    public GalleryResponseDto getUserGallery(User user) {
        UserLanguage userLanguage = userLanguageRepository.findActiveByUserIdWithLanguage(user.getId())
                .orElseThrow(() -> new NoActiveLanguageException("User with id " + user.getId() + " doesn't have an active language"));
        List<SolvedWordDto> solvedWords = userLevelProgressRepository
                .findSolvedWords(user.getId(),userLanguage.getLanguage().getId(),user.getNativeLanguage().getId());

        List<Long> worldIds = solvedWords.stream()
                .map(SolvedWordDto::getWorldId)
                .distinct()
                .toList();

        Map<Long, Map<String, String>> worldTranslations = translationResolver.resolveBatch(
                TranslatableEntityType.WORLD, worldIds, user.getNativeLanguage().getId());

        List<WordDto> wordDtos = solvedWords.stream()
                .map(solvedWord -> {
                    String worldName = worldTranslations
                            .getOrDefault(solvedWord.getWorldId(), Map.of())
                            .getOrDefault("name", solvedWord.getWorldName());
                    CustomWorldDto customWorldDto = new CustomWorldDto(solvedWord.getWorldId(), worldName, solvedWord.getWorldImageUrl());
                    return new WordDto(solvedWord.getWord(),solvedWord.getNativeWord(),customWorldDto);
                }).toList();

        return new GalleryResponseDto(wordDtos.size(),wordDtos);
    }
}