package gov.jets.iti.LinguaQuest.repository;

import gov.jets.iti.LinguaQuest.dto.SolvedWordDto;
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

    @Query(value = """
    SELECT
        w.text          AS word,
        nw.text         AS nativeWord,
        wo.id           AS worldId,
        wo.name         AS worldName,
        wo.image_url    AS worldImageUrl
    FROM user_level_progress ulp
    JOIN words w         ON w.id = ulp.word_id
    JOIN words nw        ON nw.word_code = w.word_code
                         AND nw.language_id = :nativeLanguageId
    JOIN world_levels wl ON wl.id = ulp.level_id
    JOIN worlds wo       ON wo.id = wl.world_id
    WHERE ulp.user_id = :userId
      AND w.language_id = :languageId
      AND ulp.status = 'COMPLETED'
    ORDER BY ulp.completed_at DESC
    """, nativeQuery = true)
    List<SolvedWordDto> findSolvedWords(
            @Param("userId") Long userId,
            @Param("languageId") Long languageId,
            @Param("nativeLanguageId") Long nativeLanguageId
    );
}
