package gov.jets.iti.LinguaQuest.repository;

import gov.jets.iti.LinguaQuest.entity.UserLevelProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserLevelProgressRepository extends JpaRepository<UserLevelProgress,Long> {
    @Query("""
    SELECT COUNT(ulp)
    FROM UserLevelProgress ulp
    WHERE ulp.user.id = :userId
      AND ulp.worldLevel.world.id = :worldId
      AND ulp.word.language.id = :languageId
      AND ulp.status = LevelStatus.COMPLETED
    """)
    long countCompletedLevels(
            @Param("userId") Long userId,
            @Param("worldId") Long worldId,
            @Param("languageId") Long languageId);

    @Query("""
    SELECT ulp
    FROM UserLevelProgress ulp
    WHERE ulp.user.id = :userId
      AND ulp.worldLevel.world.id = :worldId
      AND ulp.word.language.id = :languageId
    ORDER BY ulp.worldLevel.orderIndex
    """)
    List<UserLevelProgress> findUserProgressLevels(
            Long userId,
            Long worldId,
            Long languageId);

    @Query("""
    SELECT COUNT(DISTINCT ulp.worldLevel.world.id)
    FROM UserLevelProgress ulp
    WHERE ulp.user.id = :userId
      AND ulp.status = LevelStatus.COMPLETED
    """)
    int countDistinctCompletedWorldsByUserId(@Param("userId") Long userId);
}
