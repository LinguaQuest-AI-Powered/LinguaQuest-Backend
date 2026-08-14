package gov.jets.iti.LinguaQuest.service.achievement;

import gov.jets.iti.LinguaQuest.dto.reward.RewardResult;
import gov.jets.iti.LinguaQuest.entity.Achievement;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.entity.UserAchievement;
import gov.jets.iti.LinguaQuest.enums.*;
import gov.jets.iti.LinguaQuest.repository.AchievementRepository;
import gov.jets.iti.LinguaQuest.repository.UserAchievementRepository;
import gov.jets.iti.LinguaQuest.service.TranslationResolverService;
import gov.jets.iti.LinguaQuest.service.notification.NotificationService;
import gov.jets.iti.LinguaQuest.util.UserProgressUpdaterUtil;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserProgressUpdaterUtil userProgressUpdaterUtil;
    private final Map<CriteriaType, AchievementResolver> resolvers;
    private final NotificationService notificationService;
    private final MessageSource messageSource;
    private final TranslationResolverService translationResolver;

    public AchievementService(AchievementRepository achievementRepository,
                              UserAchievementRepository userAchievementRepository,
                              UserProgressUpdaterUtil userProgressUpdaterUtil,
                              List<AchievementResolver> resolverBeans,
                              NotificationService notificationService,
                              MessageSource messageSource,
                              TranslationResolverService translationResolver) {
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.userProgressUpdaterUtil = userProgressUpdaterUtil;
        this.notificationService = notificationService;
        this.messageSource = messageSource;
        this.translationResolver = translationResolver;
        this.resolvers = resolverBeans.stream()
                .collect(Collectors.toMap(AchievementResolver::supports, r -> r));
    }

    @Transactional
    public void onEvent(User user, AchievementTrigger trigger) {
        List<Achievement> candidates = achievementRepository.findByTriggerEvent(trigger);

        List<Long> candidateIds = candidates.stream().map(Achievement::getId).toList();
        Map<Long, Map<String, String>> translations = translationResolver.resolveBatch(
                TranslatableEntityType.ACHIEVEMENT,
                candidateIds,
                user.getNativeLanguage().getId()
        );

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

                Map<String, String> translatedFields = translations.getOrDefault(achievement.getId(), Map.of());
                String name = translatedFields.getOrDefault("name", achievement.getName());
                String description = translatedFields.getOrDefault("description", achievement.getDescription());

                Locale locale = Locale.forLanguageTag(user.getNativeLanguage().getCode());
                String title = messageSource.getMessage("achievement.earned.title", new Object[]{name}, locale);
                String body = buildEarnedNotificationBody(description, xp, coins, locale);

                notificationService.send(user, NotificationType.ACHIEVEMENT_EARNED, title, body);
            }

            userAchievementRepository.save(ua);
        }
    }

    private String buildEarnedNotificationBody(String description, int xp, int coins, Locale locale) {
        String rewardLine = buildRewardLine(xp, coins, locale);
        String lowered = lowerFirst(description);

        if (rewardLine.isEmpty()) {
            return messageSource.getMessage(
                    "achievement.earned.body.noReward", new Object[]{lowered}, locale);
        }
        return messageSource.getMessage(
                "achievement.earned.body.withReward", new Object[]{lowered, rewardLine}, locale);
    }


    private String lowerFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return Character.toLowerCase(text.charAt(0)) + text.substring(1);
    }
    private String buildRewardLine(int xp, int coins, Locale locale) {
        if (xp > 0 && coins > 0) {
            return messageSource.getMessage(
                    "achievement.earned.reward.both", new Object[]{coins, xp}, locale);
        }
        if (coins > 0) {
            return messageSource.getMessage(
                    "achievement.earned.reward.coinsOnly", new Object[]{coins}, locale);
        }
        if (xp > 0) {
            return messageSource.getMessage(
                    "achievement.earned.reward.xpOnly", new Object[]{xp}, locale);
        }
        return "";
    }
}