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

import java.util.ArrayList;
import java.util.Comparator;
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
        List<World> worldDtoList;
        if(difficulty == Difficulty.ALL) {
            worldDtoList = worldRepository.findAll();
        }else {
            worldDtoList = worldRepository.findWorldByDifficulty(difficulty);
        }
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

    public WorldLevelsResponseDto getWorldLevels(Long userId, Long worldId) {

        UserLanguage userLanguage = getActiveUserLanguage(userId);
        World world = getWorld(worldId);

        List<UserLevelProgress> progressLevels =
                userLevelProgressRepository.findUserProgressLevels(
                        userId,
                        worldId,
                        userLanguage.getLanguage().getId());

        List<WorldLevel> worldLevels = worldLevelRepository.findWorldLevels(worldId);

        List<LevelDto> levels = buildLevelDtos(worldLevels, progressLevels);

        updateAvailableLevel(levels);

        return new WorldLevelsResponseDto(
                worldId,
                world.getName(),
                world.getDifficulty(),
                levels
        );
    }

    private UserLanguage getActiveUserLanguage(Long userId) {
        return userLanguageRepository.findActiveByUserIdWithLanguage(userId)
                .orElseThrow(() ->
                        new NoActiveLanguageException(
                                "User with id " + userId + " doesn't have an active language"));
    }

    private World getWorld(Long worldId) {
        return worldRepository.findById(worldId)
                .orElseThrow(() ->
                        new WorldNotFoundException(
                                "World with id " + worldId + " does not exist"));
    }

    private List<LevelDto> buildLevelDtos(List<WorldLevel> worldLevels,
                                          List<UserLevelProgress> progressLevels) {

        Map<Long, UserLevelProgress> progressMap = progressLevels.stream()
                .collect(Collectors.toMap(
                        p -> p.getWorldLevel().getId(),
                        Function.identity()
                ));

        List<LevelDto> levels = new ArrayList<>();

        for (WorldLevel level : worldLevels) {

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

        return levels;
    }

    private void updateAvailableLevel(List<LevelDto> levels) {

        boolean hasPlayableLevel = levels.stream()
                .anyMatch(level ->
                        level.getStatus() == LevelStatus.INPROGRESS ||
                                level.getStatus() == LevelStatus.AVAILABLE);

        if (hasPlayableLevel) {
            return;
        }

        levels.stream()
                .filter(level -> level.getStatus() == LevelStatus.LOCKED)
                .findFirst()
                .ifPresent(level -> level.setStatus(LevelStatus.AVAILABLE));
    }

    public WorldsResponseDto getExploreWorldsPreview(Long userId, Long languageId, int limit) {
        List<World> allWorlds = worldRepository.findAll();
        List<WorldDto> worldDtos = new ArrayList<>();

        for (World world : allWorlds) {
            long worldLevelCount = worldLevelRepository.countWorldLevelByWorld(world);
            long worldCompletedLevels = userLevelProgressRepository.countCompletedLevels(userId, world.getId(), languageId);
            long progressPercent = worldLevelCount == 0 ? 0 : (worldCompletedLevels * 100) / worldLevelCount;
            worldDtos.add(mapWorldToWorldDto(world, worldLevelCount, worldCompletedLevels, progressPercent));
        }

        List<WorldDto> topWorlds = worldDtos.stream()
                .sorted(Comparator.comparingLong(WorldDto::completedLevels).reversed())
                .limit(limit)
                .toList();

        return new WorldsResponseDto(topWorlds.size(), topWorlds);
    }

    private WorldDto mapWorldToWorldDto(World world,long worldLevelCount, long worldCompletedLevels, long progressPercent) {
        return new WorldDto(world.getId(),world.getName(),world.getImageUrl(),world.getDifficulty(),
                progressPercent,worldLevelCount,worldCompletedLevels);
    }
}
