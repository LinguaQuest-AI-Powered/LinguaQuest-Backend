package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.dto.reward.RewardResult;
import gov.jets.iti.LinguaQuest.dto.world.*;
import gov.jets.iti.LinguaQuest.entity.*;
import gov.jets.iti.LinguaQuest.enums.AchievementTrigger;
import gov.jets.iti.LinguaQuest.enums.Difficulty;
import gov.jets.iti.LinguaQuest.enums.LevelStatus;
import gov.jets.iti.LinguaQuest.exception.language.NativeLanguageNotSetException;
import gov.jets.iti.LinguaQuest.exception.language.NoActiveLanguageException;
import gov.jets.iti.LinguaQuest.exception.world.*;
import gov.jets.iti.LinguaQuest.repository.*;
import gov.jets.iti.LinguaQuest.service.achievement.AchievementService;
import gov.jets.iti.LinguaQuest.util.RewardCalculatorUtil;
import gov.jets.iti.LinguaQuest.util.UserProgressUpdaterUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {
    
    private static final int CHANGE_WORD_COIN_COST = 50;
    private static final int HINT_COIN_COST = 30;

    private final UserLevelProgressRepository userLevelProgressRepository;
    private final UserLanguageRepository userLanguageRepository;
    private final WorldRepository worldRepository;
    private final WorldLevelRepository worldLevelRepository;
    private final WordRepository wordRepository;
    private final WorldService worldService;
    private final AIService aiService;
    private final RewardCalculatorUtil rewardCalculator;
    private final UserProgressUpdaterUtil userProgressUpdaterUtil;
    private final WordHintRepository wordHintRepository;
    private final AchievementService achievementService;

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

        User user = progress.getUser();
        if (user == null || user.getCoins() == null || user.getCoins() < CHANGE_WORD_COIN_COST) {
            throw new InsufficientCoinsException("Insufficient coins to change the word");
        }

        long unusedCount = wordRepository.countUnusedWordsExcludingCurrent(userId, worldId, languageId, currentWordId);
        if (unusedCount == 0) {
            throw new NoMoreWordsException("There are no other new words available in this world.");
        }

        int randomOffset = ThreadLocalRandom.current().nextInt((int) unusedCount);
        Page<Word> page = wordRepository.findUnusedWordsExcludingCurrent(
                userId, worldId, languageId, currentWordId, PageRequest.of(randomOffset, 1));
        Word newWord = page.getContent().getFirst();

        progress.setWord(newWord);
        user.setCoins(user.getCoins() - CHANGE_WORD_COIN_COST);

        progress.setHintUsed(false);
        return new StartLevelResponse(newWord.getText(), user.getCoins());
    }

    @Transactional
    public VerifyImageResponse verifyImage(Long userId, Long worldId, Long levelId, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new InvalidImageException("Uploaded image is empty or missing.");
        }

        UserLanguage userActiveLanguage = userLanguageRepository
                .findActiveByUserIdWithLanguage(userId) // or however you fetch the active one
                .orElseThrow(() -> new UserLanguageNotFoundException(
                        "User " + userId + " has no active language"));

        UserLevelProgress progress = userLevelProgressRepository
                .findInProgressOrCompletedByUserIdAndWorldIdAndLevelIdAndLanguageId(
                        userId, worldId, levelId, userActiveLanguage.getLanguage().getId())
                .orElseThrow(() -> new ActiveLevelNotFoundException(
                        "No in-progress or completed level found for user " + userId +
                                " in world " + worldId + ", level " + levelId));

        Word word = progress.getWord();
        User user = progress.getUser();

        UserLanguage userLanguage = userLanguageRepository
                .findByUserIdAndLanguageIdWithLanguage(userId, word.getLanguage().getId())
                .orElseThrow(() -> new UserLanguageNotFoundException(
                        "User " + userId + " has not added language " + word.getLanguage().getId()));


        log.info("AI verifying image for word: {}", word.getText());
        boolean isMatch = aiService.verifyImage(image, word.getText());
        log.info("AI verification result: {}", isMatch);

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

        achievementService.onEvent(user, AchievementTrigger.LEVEL_VERIFIED);

        return new VerifyImageResponse(
                true,
                reward.xp(),
                reward.coins(),
                user.getLevel(),
                userProgressUpdaterUtil.computeProgressPercentage(user.getXp()));
    }

    @Transactional
    public HintResponse getHint(Long userId, Long worldId, Long levelId) {
        worldRepository.findById(worldId)
                .orElseThrow(() -> new WorldNotFoundException("World with id " + worldId + " does not exist"));
        worldLevelRepository.findByIdAndWorldId(levelId, worldId)
                .orElseThrow(() -> new LevelNotFoundException("Level with id " + levelId + " does not exist in world " + worldId));

        UserLanguage userActiveLanguage = userLanguageRepository
                .findActiveByUserIdWithLanguage(userId)
                .orElseThrow(() -> new UserLanguageNotFoundException(
                        "User " + userId + " has no active language"));

        UserLevelProgress progress = userLevelProgressRepository
                .findInProgressOrCompletedByUserIdAndWorldIdAndLevelIdAndLanguageId(
                        userId, worldId, levelId, userActiveLanguage.getLanguage().getId())
                .orElseThrow(() -> new ActiveLevelNotFoundException(
                        "No in-progress or completed level found for user " + userId +
                                " in world " + worldId + ", level " + levelId));

        if(progress.isHintUsed()){
            throw new HintAlreadyUsedException("You have already used your hint for this level");
        }
        User user = progress.getUser();
        if (user.getCoins() == null || user.getCoins() < HINT_COIN_COST) {
            throw new InsufficientCoinsException("You do not have enough coins to buy a hint");
        }
        Language nativeLanguage = user.getNativeLanguage();
        if (nativeLanguage == null) {
            throw new NativeLanguageNotSetException("User has no native language set");
        }
        Word word = progress.getWord();
        WordHint wordHint = wordHintRepository
                .findByWordCodeAndLanguageId(word.getWordCode(), nativeLanguage.getId())
                .orElseGet(() -> {
                    String hintText = aiService.generateHint(word.getText(), nativeLanguage.getName());
                    WordHint newHint = WordHint.builder()
                            .wordCode(word.getWordCode())
                            .language(nativeLanguage)
                            .hintText(hintText)
                            .build();
                    return wordHintRepository.save(newHint);
                });
        user.setCoins(user.getCoins() - HINT_COIN_COST);
        progress.setHintUsed(true);

        return new HintResponse(
                wordHint.getHintText(),
                HINT_COIN_COST,
                user.getCoins()
        );
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
