package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
