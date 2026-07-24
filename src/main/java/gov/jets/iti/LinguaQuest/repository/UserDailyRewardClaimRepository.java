package gov.jets.iti.LinguaQuest.repository;

import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.entity.UserDailyRewardClaim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface UserDailyRewardClaimRepository extends JpaRepository<UserDailyRewardClaim,Long> {
    Optional<UserDailyRewardClaim> findByUserAndClaimedDate(User user, LocalDate claimedDate);
}
