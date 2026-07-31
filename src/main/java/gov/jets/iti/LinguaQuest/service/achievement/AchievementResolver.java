package gov.jets.iti.LinguaQuest.service.achievement;

import gov.jets.iti.LinguaQuest.entity.Achievement;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.enums.CriteriaType;

public interface AchievementResolver {
    CriteriaType supports();
    int resolveCurrentValue(User user, Achievement achievement);
}