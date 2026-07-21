package gov.jets.iti.LinguaQuest.repository;

import gov.jets.iti.LinguaQuest.entity.World;
import gov.jets.iti.LinguaQuest.enums.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorldRepository extends JpaRepository<World,Long> {

    List<World> findWorldByDifficulty(Difficulty difficulty);
}

