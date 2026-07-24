package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.dto.RewardResult;
import gov.jets.iti.LinguaQuest.dto.response.LevelDto;
import gov.jets.iti.LinguaQuest.dto.response.StartLevelResponse;
import gov.jets.iti.LinguaQuest.dto.response.VerifyImageResponse;
import gov.jets.iti.LinguaQuest.dto.response.WorldLevelsResponseDto;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.entity.UserLanguage;
import gov.jets.iti.LinguaQuest.entity.UserLevelProgress;
import gov.jets.iti.LinguaQuest.entity.Word;
import gov.jets.iti.LinguaQuest.entity.World;
import gov.jets.iti.LinguaQuest.entity.WorldLevel;
import gov.jets.iti.LinguaQuest.enums.Difficulty;
import gov.jets.iti.LinguaQuest.enums.LevelStatus;
import gov.jets.iti.LinguaQuest.exception.language.NoActiveLanguageException;
import gov.jets.iti.LinguaQuest.exception.world.ActiveLevelNotFoundException;
import gov.jets.iti.LinguaQuest.exception.world.InvalidImageException;
import gov.jets.iti.LinguaQuest.exception.world.LevelAlreadyCompletedException;
import gov.jets.iti.LinguaQuest.exception.world.LevelLockedException;
import gov.jets.iti.LinguaQuest.exception.world.LevelNotFoundException;
import gov.jets.iti.LinguaQuest.exception.world.NoMoreWordsException;
import gov.jets.iti.LinguaQuest.exception.world.ProgressNotFoundException;
import gov.jets.iti.LinguaQuest.exception.world.UserLanguageNotFoundException;
import gov.jets.iti.LinguaQuest.exception.world.WorldCompletedException;
import gov.jets.iti.LinguaQuest.exception.world.WorldNotFoundException;
import gov.jets.iti.LinguaQuest.repository.UserLanguageRepository;
import gov.jets.iti.LinguaQuest.repository.UserLevelProgressRepository;
import gov.jets.iti.LinguaQuest.repository.WordRepository;
import gov.jets.iti.LinguaQuest.repository.WorldLevelRepository;
import gov.jets.iti.LinguaQuest.repository.WorldRepository;
import gov.jets.iti.LinguaQuest.util.RewardCalculatorUtil;
import gov.jets.iti.LinguaQuest.util.UserProgressUpdaterUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class GameService {
    
    private final UserLevelProgressRepository userLevelProgressRepository;
    private final UserLanguageRepository userLanguageRepository;
    private final WorldRepository worldRepository;
    private final WorldLevelRepository worldLevelRepository;
    private final WordRepository wordRepository;
    private final WorldService worldService;
    private final AIService aiService;
    private final RewardCalculatorUtil rewardCalculator;
    private final UserProgressUpdaterUtil userProgressUpdaterUtil;

    @Transactional
    public StartLevelResponse startLevel(Long userId, Long worldId, Long levelId) {
        UserLanguage activeUserLanguage = userLanguageRepository.findActiveByUserIdWithLanguage(userId)
                .orElseThrow(() -> new NoActiveLanguageException("User with id " + userId + " doesn't have an active language"));

        Long languageId = activeUserLanguage.getLanguage().getId();

        World world = worldRepository.findById(worldId)
                .orElseThrow(() -> new WorldNotFoundException("World with id " + worldId + " does not exist"));

        WorldLevel worldLevel = worldLevelRepository.findByIdAndWorldId(levelId, worldId)
                .orElseThrow(() -> new LevelNotFoundException("Level with id " + levelId + " does not exist in world " + worldId));

        // Return existing word if level has already been started or completed
        Optional<UserLevelProgress> existingProgress = userLevelProgressRepository
                .findByUserIdAndLevelIdAndLanguageId(userId, levelId, languageId);

        // INPROGRESS or COMPLETED
        if (existingProgress.isPresent()) {
            return new StartLevelResponse(existingProgress.get().getWord().getText());
        }

        // Verify level is not locked
        WorldLevelsResponseDto worldLevelsDto = worldService.getWorldLevels(userId, worldId);

        LevelDto targetLevel = worldLevelsDto.levels().stream()
                .filter(l -> l.getId().equals(levelId))
                .findFirst()
                .orElseThrow(() -> new LevelNotFoundException("Level with id " + levelId + " does not exist in world " + worldId));

        if (targetLevel.getStatus() == LevelStatus.LOCKED) {
            throw new LevelLockedException("This level is locked. Complete previous levels first.");
        }

        // Assign a random unused word
        long unusedCount = wordRepository.countUnusedWords(userId, worldId, languageId);
        if (unusedCount == 0) {
            throw new WorldCompletedException("You have learned all words in this world!");
        }

        int randomOffset = ThreadLocalRandom.current().nextInt((int) unusedCount);
        Page<Word> page = wordRepository.findUnusedWords(userId, worldId, languageId, PageRequest.of(randomOffset, 1));
        Word word = page.getContent().getFirst();

        UserLevelProgress progress = UserLevelProgress.builder()
                .user(activeUserLanguage.getUser())
                .worldLevel(worldLevel)
                .status(LevelStatus.INPROGRESS)
                .word(word)
                .build();

        userLevelProgressRepository.save(progress);

        return new StartLevelResponse(word.getText());
    }

    @Transactional
    public StartLevelResponse changeWord(Long userId, Long worldId, Long levelId) {
        UserLanguage activeUserLanguage = userLanguageRepository.findActiveByUserIdWithLanguage(userId)
                .orElseThrow(() -> new NoActiveLanguageException("User with id " + userId + " doesn't have an active language"));

        Long languageId = activeUserLanguage.getLanguage().getId();

        worldRepository.findById(worldId)
                .orElseThrow(() -> new WorldNotFoundException("World with id " + worldId + " does not exist"));

        worldLevelRepository.findByIdAndWorldId(levelId, worldId)
                .orElseThrow(() -> new LevelNotFoundException("Level with id " + levelId + " does not exist in world " + worldId));

        UserLevelProgress progress = userLevelProgressRepository
                .findByUserIdAndLevelIdAndLanguageId(userId, levelId, languageId)
                .orElseThrow(() -> new ProgressNotFoundException("You haven't started this level yet"));

        if (progress.getStatus() == LevelStatus.COMPLETED) {
            throw new LevelAlreadyCompletedException("Level is already completed");
        }

        if (progress.getStatus() != LevelStatus.INPROGRESS) {
            throw new ProgressNotFoundException("You haven't started this level yet");
        }

        Long currentWordId = progress.getWord().getId();

        long unusedCount = wordRepository.countUnusedWordsExcludingCurrent(userId, worldId, languageId, currentWordId);
        if (unusedCount == 0) {
            throw new NoMoreWordsException("There are no other new words available in this world.");
        }

        int randomOffset = ThreadLocalRandom.current().nextInt((int) unusedCount);
        Page<Word> page = wordRepository.findUnusedWordsExcludingCurrent(
                userId, worldId, languageId, currentWordId, PageRequest.of(randomOffset, 1));
        Word newWord = page.getContent().getFirst();

        progress.setWord(newWord);

        return new StartLevelResponse(newWord.getText());
    }

    @Transactional
    public VerifyImageResponse verifyImage(Long userId, Long worldId, Long levelId, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new InvalidImageException("Uploaded image is empty or missing.");
        }

        UserLevelProgress progress = userLevelProgressRepository
                .findInProgressOrCompletedByUserIdAndWorldIdAndLevelId(userId, worldId, levelId)
                .orElseThrow(() -> new ActiveLevelNotFoundException(
                        "No in-progress or completed level found for user " + userId +
                                " in world " + worldId + ", level " + levelId));

        Word word = progress.getWord();
        User user = progress.getUser();

        UserLanguage userLanguage = userLanguageRepository
                .findByUserIdAndLanguageIdWithLanguage(userId, word.getLanguage().getId())
                .orElseThrow(() -> new UserLanguageNotFoundException(
                        "User " + userId + " has not added language " + word.getLanguage().getId()));

        boolean isMatch = aiService.verifyImage(image, word.getText());

        if (!isMatch) {
            return new VerifyImageResponse(
                    false, 0, 0,
                    user.getLevel(),
                    userProgressUpdaterUtil.computeProgressPercentage(user.getXp()));
        }

        if(progress.getStatus() == LevelStatus.COMPLETED) {
            return new VerifyImageResponse(
                    true,
                    0,
                    0,
                    user.getLevel(),
                    userProgressUpdaterUtil.computeProgressPercentage(user.getXp()));
        }
        Difficulty worldDifficulty = progress.getWorldLevel().getWorld().getDifficulty();
        RewardResult reward = rewardCalculator.calculate(word.getDifficulty(), worldDifficulty);

        progress.setStatus(LevelStatus.COMPLETED);
        progress.setCompletedAt(LocalDateTime.now());

        applyLanguageProgress(userLanguage, reward);
        userProgressUpdaterUtil.applyReward(user, reward);
        userProgressUpdaterUtil.updateDailyStreak(user);

        return new VerifyImageResponse(
                true,
                reward.xp(),
                reward.coins(),
                user.getLevel(),
                userProgressUpdaterUtil.computeProgressPercentage(user.getXp()));
    }

    private void applyLanguageProgress(UserLanguage userLanguage, RewardResult reward) {
        int newXp = userLanguage.getCurrentXp() + reward.xp();
        userLanguage.setWordsLearned(userLanguage.getWordsLearned() + 1);

        while (newXp >= userLanguage.getNextMilestoneXp()) {
            newXp -= userLanguage.getNextMilestoneXp();
            userLanguage.setLevel(userLanguage.getLevel() + 1);
            userLanguage.setLevelsCompleted(userLanguage.getLevelsCompleted() + 1);
            userLanguage.setNextMilestoneXp(userLanguage.getNextMilestoneXp() + 500);
        }

        userLanguage.setCurrentXp(newXp);
        userLanguage.setProgressPercent(
                (int) Math.round((newXp * 100.0) / userLanguage.getNextMilestoneXp()));
    }
}
