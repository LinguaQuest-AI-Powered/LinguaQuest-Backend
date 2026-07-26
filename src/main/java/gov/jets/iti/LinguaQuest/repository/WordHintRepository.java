package gov.jets.iti.LinguaQuest.repository;

import gov.jets.iti.LinguaQuest.entity.WordHint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WordHintRepository extends JpaRepository<WordHint, Long> {

    Optional<WordHint> findByWordCodeAndLanguageId(String wordCode, Long languageId);
}
