package gov.jets.iti.LinguaQuest.repository;

import gov.jets.iti.LinguaQuest.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    Optional<UserAchievement> findByUserIdAndAchievementId(Long userId, Long achievementId);

    @Query("""
        SELECT a, ua FROM Achievement a
        LEFT JOIN UserAchievement ua ON ua.achievement = a AND ua.user.id = :userId
        """)
    List<Object[]> findAllWithUserProgress(@Param("userId") Long userId);
}