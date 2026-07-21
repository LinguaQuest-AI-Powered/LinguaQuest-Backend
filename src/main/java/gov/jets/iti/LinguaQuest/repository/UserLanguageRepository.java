package gov.jets.iti.LinguaQuest.repository;

import gov.jets.iti.LinguaQuest.entity.Language;
import gov.jets.iti.LinguaQuest.entity.UserLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserLanguageRepository extends JpaRepository<UserLanguage,Long> {

    @Query("SELECT ul.language.id FROM UserLanguage ul WHERE ul.user.id = :userId")
    Set<Long> findLanguageIdsByUserId(@Param("userId") Long userId);
    @Query("""
           SELECT ul.language
           FROM UserLanguage ul
           WHERE ul.user.id = :userId
           """)
    Set<Language> findLanguageByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT ul FROM UserLanguage ul
            JOIN FETCH ul.language
            WHERE ul.user.id = :userId
            ORDER BY ul.isActive DESC, ul.level DESC
            """)
    List<UserLanguage> findAllByUserIdWithLanguage(@Param("userId") Long userId);

    @Query("""
            SELECT ul FROM UserLanguage ul
            JOIN FETCH ul.language
            WHERE ul.user.id = :userId AND ul.language.id = :languageId
            """)
    Optional<UserLanguage> findByUserIdAndLanguageIdWithLanguage(
            @Param("userId") Long userId, @Param("languageId") Long languageId);

    @Query("""
            SELECT ul FROM UserLanguage ul
            JOIN FETCH ul.language
            WHERE ul.user.id = :userId AND ul.isActive = true
            """)
    Optional<UserLanguage> findActiveByUserIdWithLanguage(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE UserLanguage ul SET ul.isActive = false WHERE ul.user.id = :userId AND ul.id <> :excludeId")
    void deactivateAllExcept(@Param("userId") Long userId, @Param("excludeId") Long excludeId);
}
