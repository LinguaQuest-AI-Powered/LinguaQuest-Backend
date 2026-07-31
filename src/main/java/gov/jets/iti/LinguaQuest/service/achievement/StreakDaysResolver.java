package gov.jets.iti.LinguaQuest.service.achievement;

import gov.jets.iti.LinguaQuest.entity.Achievement;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.enums.CriteriaType;
import org.springframework.stereotype.Component;

@Component
public class StreakDaysResolver implements AchievementResolver {
    @Override
    public CriteriaType supports() { return CriteriaType.STREAK_DAYS; }

    @Override
    public int resolveCurrentValue(User user, Achievement achievement) {
        return user.getCurrentStreakDays();
    }
}