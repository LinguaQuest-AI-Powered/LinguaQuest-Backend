package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.dto.mission.response.DailyMissionResponse;
import gov.jets.iti.LinguaQuest.entity.UserLanguage;
import gov.jets.iti.LinguaQuest.entity.Word;
import gov.jets.iti.LinguaQuest.exception.DailyMissionWordNotFound;
import gov.jets.iti.LinguaQuest.exception.language.NoActiveLanguageException;
import gov.jets.iti.LinguaQuest.exception.language.WordNotFoundException;
import gov.jets.iti.LinguaQuest.repository.LanguageRepository;
import gov.jets.iti.LinguaQuest.repository.UserLanguageRepository;
import gov.jets.iti.LinguaQuest.repository.WordRepository;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DailyMissionService {

    private final WordRepository wordRepository;
    private final StringRedisTemplate redisTemplate;
    private final LanguageRepository languageRepository;
    private final UserLanguageRepository userLanguageRepository;
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
    }

    private void saveDailyWord(String newWord) {
        redisTemplate.opsForValue().set(DAILY_WORD_KEY, newWord, Duration.ofHours(24));
    }

    private String getDailyWord() {
        return redisTemplate.opsForValue().get(DAILY_WORD_KEY);
    }
}
