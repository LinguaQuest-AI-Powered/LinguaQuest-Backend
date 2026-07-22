package gov.jets.iti.LinguaQuest.service;


import gov.jets.iti.LinguaQuest.dto.response.HomeResponse;
import gov.jets.iti.LinguaQuest.dto.response.UserLanguageDto;
import gov.jets.iti.LinguaQuest.dto.response.WorldsResponseDto;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.entity.UserLanguage;
import gov.jets.iti.LinguaQuest.exception.auth.EmailNotFoundException;
import gov.jets.iti.LinguaQuest.repository.UserLanguageRepository;
import gov.jets.iti.LinguaQuest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final UserRepository userRepository;
    private final UserLanguageRepository userLanguageRepository;
    private final WorldService worldService;

    public HomeResponse getHome(Long userId){
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EmailNotFoundException("User not found"));
        Optional<UserLanguage> userLanguageOpt = userLanguageRepository.findActiveByUserIdWithLanguage(userId);

        UserLanguageDto activeLanguage = null;
        WorldsResponseDto exploreWorlds = new WorldsResponseDto(0, List.of());

        if (userLanguageOpt.isPresent()) {
            UserLanguage userLanguage = userLanguageOpt.get();

            exploreWorlds = worldService.getExploreWorldsPreview(userId, userLanguage.getLanguage().getId(), 2);
            activeLanguage  = new UserLanguageDto(
                    userLanguage.getLanguage().getId(),
                    userLanguage.getLanguage().getName(),
                    userLanguage.getLanguage().getCode(),
                    userLanguage.getLanguage().getImageUrl(),
                    userLanguage.getLevel(),
                    userLanguage.isActive(),
                    userLanguage.getProgressPercent()
            );

        }

        return new HomeResponse(
                user.getXp(),
                user.getCoins(),
                user.getCurrentStreakDays(),
                activeLanguage,
                exploreWorlds
        );
    }

}
