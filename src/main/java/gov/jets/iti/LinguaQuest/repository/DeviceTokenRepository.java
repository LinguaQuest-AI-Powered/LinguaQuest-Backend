package gov.jets.iti.LinguaQuest.repository;

import gov.jets.iti.LinguaQuest.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    List<DeviceToken> findByUserId(Long userId);
    Optional<DeviceToken> findByToken(String token);
    void deleteByToken(String token);
}
