package gov.jets.iti.LinguaQuest.repository;

import gov.jets.iti.LinguaQuest.entity.Language;
import gov.jets.iti.LinguaQuest.entity.UserLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface UserLanguageRepository extends JpaRepository<UserLanguage,Long> {

    @Query("""
           SELECT ul.language
           FROM UserLanguage ul
           WHERE ul.user.id = :userId
           """)
    Set<Language> findLanguageByUserId(@Param("userId") Long userId);
}
