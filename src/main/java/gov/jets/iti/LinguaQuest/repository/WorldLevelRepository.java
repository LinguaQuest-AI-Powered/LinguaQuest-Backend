package gov.jets.iti.LinguaQuest.repository;

import gov.jets.iti.LinguaQuest.entity.World;
import gov.jets.iti.LinguaQuest.entity.WorldLevel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorldLevelRepository extends JpaRepository<WorldLevel,Long> {
    long countWorldLevelByWorld(World world);
}
