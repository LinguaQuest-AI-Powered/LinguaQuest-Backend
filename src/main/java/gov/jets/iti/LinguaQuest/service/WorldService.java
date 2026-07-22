package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.dto.response.LevelDto;
import gov.jets.iti.LinguaQuest.dto.response.WorldDto;
import gov.jets.iti.LinguaQuest.dto.response.WorldLevelsResponseDto;
import gov.jets.iti.LinguaQuest.dto.response.WorldsResponseDto;
import gov.jets.iti.LinguaQuest.entity.UserLanguage;
import gov.jets.iti.LinguaQuest.entity.UserLevelProgress;
import gov.jets.iti.LinguaQuest.entity.World;
import gov.jets.iti.LinguaQuest.entity.WorldLevel;
import gov.jets.iti.LinguaQuest.enums.Difficulty;
import gov.jets.iti.LinguaQuest.enums.LevelStatus;
import gov.jets.iti.LinguaQuest.exception.language.InvalidLanguageIdException;
import gov.jets.iti.LinguaQuest.exception.language.NoActiveLanguageException;
import gov.jets.iti.LinguaQuest.exception.world.WorldNotFoundException;
import gov.jets.iti.LinguaQuest.repository.UserLanguageRepository;
import gov.jets.iti.LinguaQuest.repository.UserLevelProgressRepository;
import gov.jets.iti.LinguaQuest.repository.WorldLevelRepository;
import gov.jets.iti.LinguaQuest.repository.WorldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorldService {

    private final WorldRepository worldRepository;
    private final WorldLevelRepository worldLevelRepository;
    private final UserLevelProgressRepository userLevelProgressRepository;
    private final UserLanguageRepository userLanguageRepository;

    public WorldsResponseDto getAllWorlds(Long userId, Difficulty difficulty) {


        UserLanguage userLanguage = userLanguageRepository.findActiveByUserIdWithLanguage(userId)
                .orElseThrow(() -> new NoActiveLanguageException("user with Id " + userId + " doesn't have an active language"));

        List<World> worldDtoList = worldRepository.findWorldByDifficulty(difficulty);
        List<WorldDto> worldDtos = new ArrayList<>();

        for(World world : worldDtoList) {
            long worldLevelCount = worldLevelRepository.countWorldLevelByWorld(world);
            long worldCompletedLevels = userLevelProgressRepository.countCompletedLevels(userId,world.getId(),userLanguage.getLanguage().getId());
            long progressPercent = (worldCompletedLevels* 100) / worldLevelCount;
            WorldDto worldDto = mapWorldToWorldDto(world,worldLevelCount,worldCompletedLevels,progressPercent);
            worldDtos.add(worldDto);
        }
        return new WorldsResponseDto(worldDtos.size(),worldDtos);
    }

    public WorldLevelsResponseDto getWorldLevels(Long userId , Long worldId) {
        UserLanguage userLanguage = userLanguageRepository.findActiveByUserIdWithLanguage(userId)
                .orElseThrow(() -> new NoActiveLanguageException("user with Id " + userId + " doesn't have an active language"));

        World world = worldRepository.findById(worldId)
                .orElseThrow(() -> new WorldNotFoundException("World with id " + worldId + " is not exist"));

        List<UserLevelProgress> unlockedLevels = userLevelProgressRepository.findUserProgressLevels(userId,worldId,userLanguage.getLanguage().getId());
        List<WorldLevel> allLevels = worldLevelRepository.findWorldLevels(worldId);
        Map<Long, UserLevelProgress> progressMap =
                unlockedLevels.stream()
                        .collect(Collectors.toMap(
                                p -> p.getWorldLevel().getId(),
                                Function.identity()
                        ));
        List<LevelDto> levels = new ArrayList<>();
        for (WorldLevel level : allLevels) {

            UserLevelProgress progress = progressMap.get(level.getId());

            if (progress == null) {
                levels.add(new LevelDto(
                        level.getId(),
                        level.getOrderIndex(),
                        LevelStatus.LOCKED,
                        null
                ));
            } else {
                levels.add(new LevelDto(
                        level.getId(),
                        level.getOrderIndex(),
                        progress.getStatus(),
                        progress.getWord().getText()
                ));
            }
        }

        return new WorldLevelsResponseDto(worldId,world.getName(),world.getDifficulty(),levels);
    }

    private WorldDto mapWorldToWorldDto(World world,long worldLevelCount, long worldCompletedLevels, long progressPercent) {
        return new WorldDto(world.getId(),world.getName(),world.getImageUrl(),world.getDifficulty(),
                progressPercent,worldLevelCount,worldCompletedLevels);
    }
}
