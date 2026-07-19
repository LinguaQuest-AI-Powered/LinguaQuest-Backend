package gov.jets.iti.LinguaQuest.entity;

import gov.jets.iti.LinguaQuest.enums.LevelStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_level_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "level_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLevelProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = false)
    private WorldLevel worldLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LevelStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id")
    private Word word;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}