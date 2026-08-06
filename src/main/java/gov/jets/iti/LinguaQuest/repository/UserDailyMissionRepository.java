package gov.jets.iti.LinguaQuest.repository;

import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.entity.UserDailyMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface UserDailyMissionRepository extends JpaRepository<UserDailyMission,Long> {

    Optional<UserDailyMission> findByUserAndMissionDate(User user, LocalDate missionDate);
}
