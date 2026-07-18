package gov.jets.iti.LinguaQuest.repository;

import gov.jets.iti.LinguaQuest.entity.TargetLanguage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TargetLanguageRepository extends JpaRepository<TargetLanguage,Long> {
    TargetLanguage findByName(String name);
}
