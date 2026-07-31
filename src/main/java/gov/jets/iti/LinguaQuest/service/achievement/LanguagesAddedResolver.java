package gov.jets.iti.LinguaQuest.service.achievement;

import gov.jets.iti.LinguaQuest.entity.Achievement;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.enums.CriteriaType;
import gov.jets.iti.LinguaQuest.repository.UserLanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LanguagesAddedResolver implements AchievementResolver {

    private final UserLanguageRepository userLanguageRepository;

    @Override
    public CriteriaType supports() { return CriteriaType.LANGUAGES_ADDED; }

    @Override
    public int resolveCurrentValue(User user, Achievement achievement) {
        return (int) userLanguageRepository.countByUserId(user.getId());
    }
}