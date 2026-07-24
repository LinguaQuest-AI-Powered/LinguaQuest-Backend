package gov.jets.iti.LinguaQuest.repository;

import gov.jets.iti.LinguaQuest.entity.UserLevelProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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

    @Query("""
    SELECT ulp FROM UserLevelProgress ulp
    JOIN FETCH ulp.word w
    JOIN FETCH w.language
    JOIN FETCH ulp.worldLevel wl
    JOIN FETCH wl.world
    WHERE ulp.user.id = :userId
      AND wl.world.id = :worldId
      AND wl.id = :levelId
      AND ( ulp.status = LevelStatus.INPROGRESS OR ulp.status = LevelStatus.COMPLETED)
    """)
    Optional<UserLevelProgress> findInProgressOrCompletedByUserIdAndWorldIdAndLevelId(
            @Param("userId") Long userId,
            @Param("worldId") Long worldId,
            @Param("levelId") Long levelId);

    @Query("""
    SELECT ulp FROM UserLevelProgress ulp
    JOIN FETCH ulp.word w
    WHERE ulp.user.id = :userId
      AND ulp.worldLevel.id = :levelId
      AND w.language.id = :languageId
    """)
    Optional<UserLevelProgress> findByUserIdAndLevelIdAndLanguageId(
            @Param("userId") Long userId,
            @Param("levelId") Long levelId,
            @Param("languageId") Long languageId);
}
