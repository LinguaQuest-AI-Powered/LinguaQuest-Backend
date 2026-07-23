package gov.jets.iti.LinguaQuest.repository;

import gov.jets.iti.LinguaQuest.entity.Word;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WordRepository extends JpaRepository<Word, Long> {

    @Query("""
        SELECT COUNT(w) FROM Word w
        JOIN w.worlds wd
        WHERE wd.id = :worldId
          AND w.language.id = :languageId
          AND NOT EXISTS (
              SELECT 1 FROM UserLevelProgress ulp
              WHERE ulp.user.id = :userId
                AND ulp.word.id = w.id
                AND ulp.worldLevel.world.id = :worldId
          )
        """)
    long countUnusedWords(@Param("userId") Long userId,
                          @Param("worldId") Long worldId,
                          @Param("languageId") Long languageId);

    @Query("""
        SELECT w FROM Word w
        JOIN w.worlds wd
        WHERE wd.id = :worldId
          AND w.language.id = :languageId
          AND NOT EXISTS (
              SELECT 1 FROM UserLevelProgress ulp
              WHERE ulp.user.id = :userId
                AND ulp.word.id = w.id
                AND ulp.worldLevel.world.id = :worldId
          )
        """)
    Page<Word> findUnusedWords(@Param("userId") Long userId,
                               @Param("worldId") Long worldId,
                               @Param("languageId") Long languageId,
                               Pageable pageable);

    @Query("""
        SELECT COUNT(w) FROM Word w
        JOIN w.worlds wd
        WHERE wd.id = :worldId
          AND w.language.id = :languageId
          AND w.id <> :currentWordId
          AND NOT EXISTS (
              SELECT 1 FROM UserLevelProgress ulp
              WHERE ulp.user.id = :userId
                AND ulp.word.id = w.id
                AND ulp.worldLevel.world.id = :worldId
          )
        """)
    long countUnusedWordsExcludingCurrent(@Param("userId") Long userId,
                                          @Param("worldId") Long worldId,
                                          @Param("languageId") Long languageId,
                                          @Param("currentWordId") Long currentWordId);

    @Query("""
        SELECT w FROM Word w
        JOIN w.worlds wd
        WHERE wd.id = :worldId
          AND w.language.id = :languageId
          AND w.id <> :currentWordId
          AND NOT EXISTS (
              SELECT 1 FROM UserLevelProgress ulp
              WHERE ulp.user.id = :userId
                AND ulp.word.id = w.id
                AND ulp.worldLevel.world.id = :worldId
          )
        """)
    Page<Word> findUnusedWordsExcludingCurrent(@Param("userId") Long userId,
                                               @Param("worldId") Long worldId,
                                               @Param("languageId") Long languageId,
                                               @Param("currentWordId") Long currentWordId,
                                               Pageable pageable);
}
