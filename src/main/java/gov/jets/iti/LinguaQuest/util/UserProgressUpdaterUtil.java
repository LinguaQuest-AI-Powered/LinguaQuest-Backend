package gov.jets.iti.LinguaQuest.util;

import gov.jets.iti.LinguaQuest.dto.RewardResult;
import gov.jets.iti.LinguaQuest.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class UserProgressUpdaterUtil {

    private static final int LEVEL_XP_STEP = 500;


    public int computeProgressPercentage(int totalXp) {
        int xpIntoCurrentLevel = totalXp % LEVEL_XP_STEP;
        return (int) Math.round(xpIntoCurrentLevel * 100.0 / LEVEL_XP_STEP);
    }

    public void applyReward(User user, RewardResult reward) {
        int newXp = user.getXp() + reward.xp();
        user.setXp(newXp);
        user.setCoins(user.getCoins() + reward.coins());
        user.setLevel(computeLevel(newXp));
    }

    public void updateDailyStreak(User user) {
        LocalDate today = LocalDate.now();
        LocalDateTime lastActiveAt = user.getLastActiveAt();
        LocalDate lastActiveDate = lastActiveAt != null ? lastActiveAt.toLocalDate() : null;

        if (lastActiveDate == null || lastActiveDate.isBefore(today.minusDays(1))) {
            user.setCurrentStreakDays(1);
        } else if (lastActiveDate.equals(today.minusDays(1))) {
            user.setCurrentStreakDays(user.getCurrentStreakDays() + 1);
        }
        user.setLastActiveAt(LocalDateTime.now());
    }

    private int computeLevel(int totalXp) {
        return 1 + (totalXp / LEVEL_XP_STEP);
    }

}
