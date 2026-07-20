package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.dto.response.LevelDto;
import gov.jets.iti.LinguaQuest.dto.response.WorldDto;
import gov.jets.iti.LinguaQuest.dto.response.WorldLevelsResponseDto;
import gov.jets.iti.LinguaQuest.dto.response.WorldsResponseDto;
import gov.jets.iti.LinguaQuest.entity.UserLevelProgress;
import gov.jets.iti.LinguaQuest.entity.World;
import gov.jets.iti.LinguaQuest.entity.WorldLevel;
import gov.jets.iti.LinguaQuest.enums.Difficulty;
import gov.jets.iti.LinguaQuest.enums.LevelStatus;
import gov.jets.iti.LinguaQuest.exception.language.InvalidLanguageIdException;
import gov.jets.iti.LinguaQuest.exception.world.WorldNotFoundException;
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

    public WorldsResponseDto getAllWorlds(Long userId,Long languageId, Difficulty difficulty) {

        if(languageId < 1) {
            throw new InvalidLanguageIdException("Invalid languageId");
        }

        List<World> worldDtoList = worldRepository.findWorldByDifficulty(difficulty);
        List<WorldDto> worldDtos = new ArrayList<>();

        for(World world : worldDtoList) {
            long worldLevelCount = worldLevelRepository.countWorldLevelByWorld(world);
            long worldCompletedLevels = userLevelProgressRepository.countCompletedLevels(userId,world.getId(),languageId);
            long progressPercent = (worldCompletedLevels* 100) / worldLevelCount;
            WorldDto worldDto = mapWorldToWorldDto(world,worldLevelCount,worldCompletedLevels,progressPercent);
            worldDtos.add(worldDto);
        }
        return new WorldsResponseDto(worldDtos.size(),worldDtos);
    }

    public WorldLevelsResponseDto getWorldLevels(Long userId , Long worldId, Long languageId) {
        if(languageId < 1) {
            throw new InvalidLanguageIdException("Invalid languageId");
        }
        World world = worldRepository.findById(worldId)
                .orElseThrow(() -> new WorldNotFoundException("World with id " + worldId + " is not exist"));

        List<UserLevelProgress> unlockedLevels = userLevelProgressRepository.findUserProgressLevels(userId,worldId,languageId);
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
