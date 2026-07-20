package gov.jets.iti.LinguaQuest.repository;

import gov.jets.iti.LinguaQuest.entity.Language;
import gov.jets.iti.LinguaQuest.entity.UserLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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
}
