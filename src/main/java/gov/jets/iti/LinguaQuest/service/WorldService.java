package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.dto.response.WorldDto;
import gov.jets.iti.LinguaQuest.dto.response.WorldsResponseDto;
import gov.jets.iti.LinguaQuest.entity.World;
import gov.jets.iti.LinguaQuest.entity.WorldLevel;
import gov.jets.iti.LinguaQuest.enums.Difficulty;
import gov.jets.iti.LinguaQuest.exception.language.InvalidLanguageIdException;
import gov.jets.iti.LinguaQuest.repository.UserLevelProgressRepository;
import gov.jets.iti.LinguaQuest.repository.WorldLevelRepository;
import gov.jets.iti.LinguaQuest.repository.WorldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    private WorldDto mapWorldToWorldDto(World world,long worldLevelCount, long worldCompletedLevels, long progressPercent) {
        return new WorldDto(world.getId(),world.getName(),world.getImageUrl(),world.getDifficulty(),
                progressPercent,worldLevelCount,worldCompletedLevels);
    }
}
