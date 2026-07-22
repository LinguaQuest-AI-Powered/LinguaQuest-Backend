package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.dto.RewardResult;
import gov.jets.iti.LinguaQuest.dto.response.VerifyImageResponse;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.entity.UserLanguage;
import gov.jets.iti.LinguaQuest.entity.UserLevelProgress;
import gov.jets.iti.LinguaQuest.entity.Word;
import gov.jets.iti.LinguaQuest.enums.Difficulty;
import gov.jets.iti.LinguaQuest.enums.LevelStatus;
import gov.jets.iti.LinguaQuest.exception.world.ActiveLevelNotFoundException;
import gov.jets.iti.LinguaQuest.exception.world.InvalidImageException;
import gov.jets.iti.LinguaQuest.exception.world.UserLanguageNotFoundException;
import gov.jets.iti.LinguaQuest.repository.UserLanguageRepository;
import gov.jets.iti.LinguaQuest.repository.UserLevelProgressRepository;
import gov.jets.iti.LinguaQuest.util.RewardCalculatorUtil;
import gov.jets.iti.LinguaQuest.util.UserProgressUpdaterUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GameService {
    
    private final UserLevelProgressRepository userLevelProgressRepository;
    private final UserLanguageRepository userLanguageRepository;
    private final AIService aiService;
    private final RewardCalculatorUtil rewardCalculator;
    private final UserProgressUpdaterUtil userProgressUpdaterUtil;

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
