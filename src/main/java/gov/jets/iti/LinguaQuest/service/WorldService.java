package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.dto.world.*;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.entity.UserLanguage;
import gov.jets.iti.LinguaQuest.entity.UserLevelProgress;
import gov.jets.iti.LinguaQuest.entity.World;
import gov.jets.iti.LinguaQuest.entity.WorldLevel;
import gov.jets.iti.LinguaQuest.enums.Difficulty;
import gov.jets.iti.LinguaQuest.enums.LevelStatus;
import gov.jets.iti.LinguaQuest.enums.TranslatableEntityType;
import gov.jets.iti.LinguaQuest.exception.auth.EmailNotFoundException;
import gov.jets.iti.LinguaQuest.exception.language.NoActiveLanguageException;
import gov.jets.iti.LinguaQuest.exception.world.WorldNotFoundException;
import gov.jets.iti.LinguaQuest.repository.UserLanguageRepository;
import gov.jets.iti.LinguaQuest.repository.UserLevelProgressRepository;
import gov.jets.iti.LinguaQuest.repository.UserRepository;
import gov.jets.iti.LinguaQuest.repository.WorldLevelRepository;
import gov.jets.iti.LinguaQuest.repository.WorldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorldService {

    private final WorldRepository worldRepository;
    private final WorldLevelRepository worldLevelRepository;
    private final UserLevelProgressRepository userLevelProgressRepository;
    private final UserLanguageRepository userLanguageRepository;
    private final UserRepository userRepository;
    private final TranslationResolverService translationResolver;

    public WorldsResponseDto getAllWorlds(Long userId, Difficulty difficulty) {

        UserLanguage userLanguage = userLanguageRepository.findActiveByUserIdWithLanguage(userId)
                .orElseThrow(() -> new NoActiveLanguageException("user with Id " + userId + " doesn't have an active language"));

        Long nativeLanguageId = getUserNativeLanguageId(userId);

        List<World> worldDtoList;
        if(difficulty == Difficulty.ALL) {
            worldDtoList = worldRepository.findAll();
        }else {
            worldDtoList = worldRepository.findWorldByDifficulty(difficulty);
        }

        List<Long> worldIds = worldDtoList.stream().map(World::getId).toList();
        Map<Long, Map<String, String>> translations =
                translationResolver.resolveBatch(TranslatableEntityType.WORLD, worldIds, nativeLanguageId);

        List<WorldDto> worldDtos = new ArrayList<>();

        for(World world : worldDtoList) {
            long worldLevelCount = worldLevelRepository.countWorldLevelByWorld(world);
            long worldCompletedLevels = userLevelProgressRepository.countCompletedLevels(userId,world.getId(),userLanguage.getLanguage().getId());
            long progressPercent = worldLevelCount != 0 ? ((worldCompletedLevels* 100) / worldLevelCount) : 0 ;

            WorldDto worldDto = mapWorldToWorldDto(world,worldLevelCount,worldCompletedLevels,progressPercent,translations);
            worldDtos.add(worldDto);
        }
        return new WorldsResponseDto(worldDtos.size(),worldDtos);
    }

    public WorldLevelsResponseDto getWorldLevels(Long userId, Long worldId) {

        UserLanguage userLanguage = getActiveUserLanguage(userId);
        World world = getWorld(worldId);

        Long nativeLanguageId = getUserNativeLanguageId(userId);
        String worldName = translationResolver
                .resolveBatch(TranslatableEntityType.WORLD, List.of(worldId), nativeLanguageId)
                .getOrDefault(worldId, Map.of())
                .getOrDefault("name", world.getName());

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
                worldName,
                world.getDifficulty(),
                levels
        );
    }

    private Long getUserNativeLanguageId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EmailNotFoundException("User not found"));
        return user.getNativeLanguage().getId();
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
        List<Long> orderedWorldIds = userLevelProgressRepository
                .findWorldIdsOrderedByRecentActivity(userId, languageId);

        List<World> allWorlds = worldRepository.findAll();
        Map<Long, World> worldsById = allWorlds.stream()
                .collect(Collectors.toMap(World::getId, w -> w));

        Long nativeLanguageId = getUserNativeLanguageId(userId);
        List<Long> allWorldIds = allWorlds.stream().map(World::getId).toList();
        Map<Long, Map<String, String>> translations =
                translationResolver.resolveBatch(TranslatableEntityType.WORLD, allWorldIds, nativeLanguageId);

        Map<Long, Long> totalLevelsByWorld = worldLevelRepository.countLevelsGroupedByWorld().stream()
                .collect(Collectors.toMap(WorldLevelCountView::getWorldId, WorldLevelCountView::getCnt));

        Map<Long, Long> completedLevelsByWorld = userLevelProgressRepository
                .countCompletedLevelsGroupedByWorld(userId, languageId).stream()
                .collect(Collectors.toMap(WorldLevelCountView::getWorldId, WorldLevelCountView::getCnt));

        List<WorldDto> topWorlds = new ArrayList<>();
        Set<Long> consideredWorldIds = new HashSet<>();

        for (Long worldId : orderedWorldIds) {
            if (topWorlds.size() == limit) break;

            World world = worldsById.get(worldId);
            if (world == null) continue;

            long worldLevelCount = totalLevelsByWorld.getOrDefault(worldId, 0L);
            long worldCompletedLevels = completedLevelsByWorld.getOrDefault(worldId, 0L);
            long progressPercent = worldLevelCount == 0 ? 0 : (worldCompletedLevels * 100) / worldLevelCount;

            consideredWorldIds.add(worldId);
            if (progressPercent >= 100) continue;

            topWorlds.add(mapWorldToWorldDto(world, worldLevelCount, worldCompletedLevels, progressPercent, translations));
        }

        if (topWorlds.size() < limit) {
            for (World world : allWorlds) {
                if (topWorlds.size() == limit) break;
                if (consideredWorldIds.contains(world.getId())) continue;

                long worldLevelCount = totalLevelsByWorld.getOrDefault(world.getId(), 0L);
                topWorlds.add(mapWorldToWorldDto(world, worldLevelCount, 0L, 0L, translations));
            }
        }

        if (topWorlds.isEmpty()) {
            for (Long worldId : orderedWorldIds) {
                if (topWorlds.size() == limit) break;

                World world = worldsById.get(worldId);
                if (world == null) continue;

                long worldLevelCount = totalLevelsByWorld.getOrDefault(worldId, 0L);
                long worldCompletedLevels = completedLevelsByWorld.getOrDefault(worldId, 0L);
                long progressPercent = worldLevelCount == 0 ? 0 : (worldCompletedLevels * 100) / worldLevelCount;

                topWorlds.add(mapWorldToWorldDto(world, worldLevelCount, worldCompletedLevels, progressPercent, translations));
            }
        }

        return new WorldsResponseDto(topWorlds.size(), topWorlds);
    }

    public Optional<ContinueLevelDto> getContinueTarget(Long userId) {
        UserLanguage activeUserLanguage = getActiveUserLanguage(userId);
        Long languageId = activeUserLanguage.getLanguage().getId();

        List<Long> worldIds = userLevelProgressRepository
                .findWorldIdsOrderedByRecentActivity(userId, languageId);

        for (Long worldId : worldIds) {
            WorldLevelsResponseDto worldLevels = getWorldLevels(userId, worldId);

            Optional<LevelDto> target = worldLevels.levels().stream()
                    .filter(l -> l.getStatus() == LevelStatus.INPROGRESS
                            || l.getStatus() == LevelStatus.AVAILABLE)
                    .findFirst();

            if (target.isPresent()) {
                LevelDto level = target.get();
                return Optional.of(new ContinueLevelDto(
                        worldId,
                        worldLevels.name(),
                        level.getId(),
                        level.getOrder(),
                        level.getWord()
                ));
            }
        }

        return Optional.empty();
    }

    private WorldDto mapWorldToWorldDto(World world, long worldLevelCount, long worldCompletedLevels,
                                        long progressPercent, Map<Long, Map<String, String>> translations) {
        String name = translations.getOrDefault(world.getId(), Map.of())
                .getOrDefault("name", world.getName());
        return new WorldDto(world.getId(), name, world.getImageUrl(), world.getDifficulty(),
                progressPercent, worldLevelCount, worldCompletedLevels);
    }
}