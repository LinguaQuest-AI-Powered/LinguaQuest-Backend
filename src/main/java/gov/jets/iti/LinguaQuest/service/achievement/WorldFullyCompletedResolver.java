package gov.jets.iti.LinguaQuest.service.achievement;

import gov.jets.iti.LinguaQuest.entity.Achievement;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.enums.CriteriaType;
import gov.jets.iti.LinguaQuest.repository.UserLevelProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorldFullyCompletedResolver implements AchievementResolver {

    private final UserLevelProgressRepository userLevelProgressRepository;

    @Override
    public CriteriaType supports() { return CriteriaType.WORLD_FULLY_COMPLETED; }

    @Override
    public int resolveCurrentValue(User user, Achievement achievement) {
        Integer maxAcrossLanguages = userLevelProgressRepository
                .findMaxCompletedLevelsInWorldAcrossLanguages(user.getId(), achievement.getTargetWorldId());
        return maxAcrossLanguages != null ? maxAcrossLanguages : 0;
    }
}