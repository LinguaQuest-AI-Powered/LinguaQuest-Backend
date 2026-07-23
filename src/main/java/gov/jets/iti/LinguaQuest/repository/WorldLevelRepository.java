package gov.jets.iti.LinguaQuest.repository;

import gov.jets.iti.LinguaQuest.entity.World;
import gov.jets.iti.LinguaQuest.entity.WorldLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorldLevelRepository extends JpaRepository<WorldLevel,Long> {
    long countWorldLevelByWorld(World world);

    Optional<WorldLevel> findByIdAndWorldId(Long id, Long worldId);

    @Query("""
        SELECT wl
        FROM WorldLevel wl
        WHERE wl.world.id = :worldId
        ORDER BY wl.orderIndex
        """)
    List<WorldLevel> findWorldLevels(Long worldId);
}
