package gov.jets.iti.LinguaQuest.repository;

import gov.jets.iti.LinguaQuest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    void markEmailVerified(String email);
}
