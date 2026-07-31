package gov.jets.iti.LinguaQuest.service.achievement;

import gov.jets.iti.LinguaQuest.entity.Achievement;
import gov.jets.iti.LinguaQuest.entity.Language;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.entity.UserLanguage;
import gov.jets.iti.LinguaQuest.enums.CriteriaType;
import gov.jets.iti.LinguaQuest.repository.UserLanguageRepository;
import gov.jets.iti.LinguaQuest.repository.UserLevelProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LessonsCompletedInWorldResolver implements AchievementResolver {

    private final UserLevelProgressRepository userLevelProgressRepository;
    private final UserLanguageRepository userLanguageRepository;

    @Override
    public CriteriaType supports() { return CriteriaType.LESSONS_COMPLETED_IN_WORLD; }

    @Override
    public int resolveCurrentValue(User user, Achievement achievement) {
        Long activeLanguageId = userLanguageRepository
                .findActiveByUserIdWithLanguage(user.getId())
                .map(UserLanguage::getLanguage)
                .map(Language::getId)
                .orElse(null);

        if (activeLanguageId == null) return 0;

        return (int) userLevelProgressRepository.countCompletedLevels(
                user.getId(),
                achievement.getTargetWorldId(),
                activeLanguageId
        );
    }
}