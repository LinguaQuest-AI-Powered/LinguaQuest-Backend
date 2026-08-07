package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.dto.mission.response.DailyMissionResponse;
import gov.jets.iti.LinguaQuest.dto.mission.response.DailyMissionVerificationResponse;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.entity.UserDailyMission;
import gov.jets.iti.LinguaQuest.entity.UserLanguage;
import gov.jets.iti.LinguaQuest.entity.Word;
import gov.jets.iti.LinguaQuest.enums.NotificationType;
import gov.jets.iti.LinguaQuest.exception.DailyMissionSolvedException;
import gov.jets.iti.LinguaQuest.exception.DailyMissionWordNotFound;
import gov.jets.iti.LinguaQuest.exception.language.NoActiveLanguageException;
import gov.jets.iti.LinguaQuest.exception.language.WordNotFoundException;
import gov.jets.iti.LinguaQuest.exception.world.InvalidImageException;
import gov.jets.iti.LinguaQuest.repository.*;
import gov.jets.iti.LinguaQuest.service.notification.NotificationService;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyMissionService {

    private final WordRepository wordRepository;
    private final StringRedisTemplate redisTemplate;
    private final UserLanguageRepository userLanguageRepository;
    private final UserDailyMissionRepository userDailyMissionRepository;
    private final AIService aiService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final Integer XPEARNED = 25;
    private final Integer COINSEARNED = 25;


    private static final String DAILY_WORD_KEY = "DAILY_WORD_CODE_KEY";

    public String getDailyWord(UserPrinciple userPrinciple) {
        String dailyWordCode = getDailyWord();
        if(dailyWordCode == null) {
            throw new DailyMissionWordNotFound("Cannot find a daily mission for today");
        }
        UserLanguage userLanguage = userLanguageRepository.findActiveByUserIdWithLanguage(userPrinciple.user().getId())
                .orElseThrow(() -> new NoActiveLanguageException("user with Id " + userPrinciple.user().getId() + " doesn't have an active language"));

        Word word = wordRepository.findWordByWordCodeAndLanguage(dailyWordCode, userLanguage.getLanguage())
                .orElseThrow(() -> new WordNotFoundException("Word Not found in your Active language"));
        return word.getText();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void generateTodayWord() {
        Optional<Word> newWord = wordRepository.findRandomWordByLanguage(1L);
        newWord.ifPresent(word -> saveDailyWord(word.getWordCode()));
        notificationService.broadcastNotification(NotificationType.DAILY_MISSION_AVAILABLE,"New Daily Mission!","A fresh word is waiting for you. Snap a photo and earn XP + coins!");
    }

    @Transactional
    public DailyMissionVerificationResponse verifyDailyMission(MultipartFile image, String word, User user) {
        if (image == null || image.isEmpty()) {
            throw new InvalidImageException("Uploaded image is empty or missing.");
        }
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Optional<UserDailyMission> userDailyMissionOptional = userDailyMissionRepository.findByUserAndMissionDate(user,today);
        if(userDailyMissionOptional.isPresent()){
            throw new DailyMissionSolvedException("Daily mission solved already");
        }
        log.info("AI verifying image for word: {}", word);
        boolean isMatch = aiService.verifyImage(image,word);
        log.info("AI verification result: {}", isMatch);

        if(!isMatch) {
            return new DailyMissionVerificationResponse(isMatch,0,0);
        }
        Word word1 = wordRepository.findWordByText(word).orElseThrow(() -> new WordNotFoundException( word + " is not found"));
        UserDailyMission userDailyMission = UserDailyMission.builder()
                .missionDate(today)
                .user(user)
                .word(word1)
                .build();
        userDailyMissionRepository.save(userDailyMission);
        userRepository.updateXpAndCoins(user.getId(),user.getXp() + XPEARNED, user.getCoins() + COINSEARNED);
        return new DailyMissionVerificationResponse(isMatch,XPEARNED,COINSEARNED);
    }

    private void saveDailyWord(String newWord) {
        redisTemplate.opsForValue().set(DAILY_WORD_KEY, newWord, Duration.ofHours(24));
    }

    private String getDailyWord() {
        return redisTemplate.opsForValue().get(DAILY_WORD_KEY);
    }
}
