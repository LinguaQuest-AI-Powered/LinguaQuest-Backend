package gov.jets.iti.LinguaQuest.service.achievement;

import gov.jets.iti.LinguaQuest.dto.reward.RewardResult;
import gov.jets.iti.LinguaQuest.entity.Achievement;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.entity.UserAchievement;
import gov.jets.iti.LinguaQuest.enums.AchievementStatus;
import gov.jets.iti.LinguaQuest.enums.AchievementTrigger;
import gov.jets.iti.LinguaQuest.enums.CriteriaType;
import gov.jets.iti.LinguaQuest.repository.AchievementRepository;
import gov.jets.iti.LinguaQuest.repository.UserAchievementRepository;
import gov.jets.iti.LinguaQuest.util.UserProgressUpdaterUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserProgressUpdaterUtil userProgressUpdaterUtil;
    private final Map<CriteriaType, AchievementResolver> resolvers;

    public AchievementService(AchievementRepository achievementRepository,
                              UserAchievementRepository userAchievementRepository,
                              UserProgressUpdaterUtil userProgressUpdaterUtil,
                              List<AchievementResolver> resolverBeans) {
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.userProgressUpdaterUtil = userProgressUpdaterUtil;
        this.resolvers = resolverBeans.stream()
                .collect(Collectors.toMap(AchievementResolver::supports, r -> r));
    }

    @Transactional
    public void onEvent(User user, AchievementTrigger trigger) {
        List<Achievement> candidates = achievementRepository.findByTriggerEvent(trigger);

        for (Achievement achievement : candidates) {
            AchievementResolver resolver = resolvers.get(achievement.getCriteriaType());
            if (resolver == null) continue;

            UserAchievement ua = userAchievementRepository
                    .findByUserIdAndAchievementId(user.getId(), achievement.getId())
                    .orElseGet(() -> UserAchievement.builder()
                            .user(user)
                            .achievement(achievement)
                            .build());

            if (ua.getStatus() == AchievementStatus.EARNED) continue;

            int current = resolver.resolveCurrentValue(user, achievement);
            int percent = Math.min(100, (int) ((current * 100.0) / achievement.getTargetValue()));
            ua.setProgressPercent(percent);

            if (current >= achievement.getTargetValue()) {
                int xp = achievement.getXpReward() != null ? achievement.getXpReward() : 0;
                int coins = achievement.getCoinReward() != null ? achievement.getCoinReward() : 0;

                ua.setStatus(AchievementStatus.EARNED);
                ua.setEarnedAt(LocalDateTime.now());
                ua.setXpAwarded(xp);
                ua.setCoinAwarded(coins);

                userProgressUpdaterUtil.applyReward(user, new RewardResult(xp, coins));
            }

            userAchievementRepository.save(ua);
        }
    }
}