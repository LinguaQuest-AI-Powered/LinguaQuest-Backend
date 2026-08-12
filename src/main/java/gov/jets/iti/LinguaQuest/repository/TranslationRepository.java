package gov.jets.iti.LinguaQuest.repository;


import gov.jets.iti.LinguaQuest.entity.Translation;
import gov.jets.iti.LinguaQuest.enums.TranslatableEntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TranslationRepository extends JpaRepository<Translation, Long> {

    @Query("""
        SELECT t FROM Translation t
        WHERE t.entityType = :entityType
        AND t.entityId IN :entityIds
        AND t.language.id = :languageId
        """)
    List<Translation> findByEntityTypeAndEntityIdInAndLanguageId(
            @Param("entityType") TranslatableEntityType entityType,
            @Param("entityIds") Collection<Long> entityIds,
            @Param("languageId") Long languageId
    );
}
