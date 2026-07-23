package gov.jets.iti.LinguaQuest.repository;

import gov.jets.iti.LinguaQuest.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    Optional<User> findUserByEmail(String email);

    Optional<User> findByFirebaseUid(String firebaseUid);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.nativeLanguage WHERE u.id = :id")
    Optional<User> findByIdWithNativeLanguage(@Param("id") Long id);

    @Modifying
    @Query("UPDATE User u SET u.isVerified = true WHERE u.email = :email")
    void markEmailVerified(@Param("email") String email);

    @Query("""
        SELECT u
        FROM User u
        WHERE u.isDeleted = false
        ORDER BY u.xp DESC, u.id ASC
        """)
    Page<User> findLeaderboard(Pageable pageable);

    @Query("""
    SELECT COUNT(u) + 1
    FROM User u
    WHERE u.isDeleted = false
      AND (
            u.xp > :xp
         OR (u.xp = :xp AND u.id < :userId)
      )
    """)
    Integer findRank(Long userId, Integer xp);
}
