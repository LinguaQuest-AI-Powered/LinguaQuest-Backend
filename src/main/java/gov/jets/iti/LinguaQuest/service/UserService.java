package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void markEmailVerified(String email){
        userRepository.markEmailVerified(email);
    }

    @Transactional
    public void updatePassword(User user, String encodedPassword) {
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    /**
     * Purge unverified accounts older than 24 hours daily at 3 AM.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void purgeUnverifiedAccounts() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        int deleted = userRepository.deleteUnverifiedUsersOlderThan(cutoff);
        if (deleted > 0) {
            log.info("Purged {} unverified user accounts created before {}", deleted, cutoff);
        }
    }
}

