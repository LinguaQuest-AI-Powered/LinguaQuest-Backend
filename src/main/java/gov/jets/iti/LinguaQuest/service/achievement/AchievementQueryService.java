package gov.jets.iti.LinguaQuest.service.achievement;

import gov.jets.iti.LinguaQuest.dto.achievement.AchievementDto;
import gov.jets.iti.LinguaQuest.dto.achievement.AchievementsListData;
import gov.jets.iti.LinguaQuest.entity.Achievement;
import gov.jets.iti.LinguaQuest.entity.UserAchievement;
import gov.jets.iti.LinguaQuest.enums.AchievementStatus;
import gov.jets.iti.LinguaQuest.repository.UserAchievementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AchievementQueryService {

    private final UserAchievementRepository userAchievementRepository;

    public AchievementsListData getAchievements(Long userId, String statusFilter) {
        List<Object[]> rows = userAchievementRepository.findAllWithUserProgress(userId);

        int earnedCount = 0;
        int inProgressCount = 0;
        int xpEarned = 0;
        List<AchievementDto> dtos = new java.util.ArrayList<>();

        for (Object[] row : rows) {
            Achievement achievement = (Achievement) row[0];
            UserAchievement ua = (UserAchievement) row[1];

            AchievementStatus status = ua != null ? ua.getStatus() : AchievementStatus.LOCKED;
            int progress = ua != null ? ua.getProgressPercent() : 0;

            if (status == AchievementStatus.EARNED) {
                earnedCount++;
                xpEarned += ua.getXpAwarded() != null ? ua.getXpAwarded() : 0;
            } else if (progress > 0) {
                inProgressCount++;
            }

            boolean matchesFilter = switch (statusFilter) {
                case "EARNED" -> status == AchievementStatus.EARNED;
                case "LOCKED" -> status == AchievementStatus.LOCKED;
                default -> true;
            };
            if (!matchesFilter) continue;

            dtos.add(new AchievementDto(
                    achievement.getId(),
                    achievement.getName(),
                    achievement.getDescription(),
                    achievement.getIconUrl(),
                    status,
                    progress,
                    achievement.getTargetValue(),
                    achievement.getXpReward(),
                    achievement.getCoinReward(),
                    ua != null ? ua.getEarnedAt() : null
            ));
        }

        return new AchievementsListData(earnedCount, inProgressCount, xpEarned, dtos);
    }
}