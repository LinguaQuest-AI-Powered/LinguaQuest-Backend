package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
     * Purge unverified accounts older than 24 hours daily at 3:55 AM (Africa/Cairo time).
     */
    @Scheduled(cron = "0 55 3 * * ?", zone = "Africa/Cairo")
    @Transactional
    public void purgeUnverifiedAccounts() {
        log.info("Starting unverified accounts purge job...");
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<User> unverifiedUsers = userRepository.findByIsVerifiedFalseAndCreatedAtBefore(cutoff);
        if (!unverifiedUsers.isEmpty()) {
            userRepository.deleteAll(unverifiedUsers);
            log.info("Purged {} unverified user accounts created before {}", unverifiedUsers.size(), cutoff);
        } else {
            log.info("Unverified accounts purge job completed. 0 accounts needed purging.");
        }
    }
}

