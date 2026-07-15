package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public void markEmailVerified(String email){
        //userRepository.markEmailVerified(email);
    }

}
