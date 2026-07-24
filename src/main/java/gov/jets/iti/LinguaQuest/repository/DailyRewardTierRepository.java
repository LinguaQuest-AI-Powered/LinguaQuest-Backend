package gov.jets.iti.LinguaQuest.repository;

import gov.jets.iti.LinguaQuest.entity.DailyRewardTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DailyRewardTierRepository extends JpaRepository<DailyRewardTier,Long> {
    Optional<DailyRewardTier> findByDayNumber(Integer dayNumber);

    @Query("select MAX(t.dayNumber) from DailyRewardTier t")
    Optional<Integer> findMaxDayNumber();
}
