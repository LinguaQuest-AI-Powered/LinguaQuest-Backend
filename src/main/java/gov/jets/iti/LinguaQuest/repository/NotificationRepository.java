package gov.jets.iti.LinguaQuest.repository;

import gov.jets.iti.LinguaQuest.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    long countByUserIdAndIsReadFalse(Long userId);
    int deleteByIdAndUserId(Long id, Long userId);
    void deleteByUserId(Long userId);
}