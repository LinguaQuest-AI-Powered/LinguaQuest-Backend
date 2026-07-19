package gov.jets.iti.LinguaQuest.repository;

import gov.jets.iti.LinguaQuest.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface LanguageRepository extends JpaRepository<Language,Long> {
    Language findByName(String name);
    List<Language> findAllByOrderByNameAsc();
}
