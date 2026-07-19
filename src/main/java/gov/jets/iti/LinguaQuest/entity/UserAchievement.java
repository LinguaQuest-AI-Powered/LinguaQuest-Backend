package gov.jets.iti.LinguaQuest.entity;

import gov.jets.iti.LinguaQuest.enums.AchievementStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_achievements",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "achievement_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AchievementStatus status = AchievementStatus.LOCKED;

    @Column(name = "progress_percent", nullable = false)
    @Builder.Default
    private Integer progressPercent = 0;

    @Column(name = "xp_awarded")
    private Integer xpAwarded;

    @Column(name = "earned_at")
    private LocalDateTime earnedAt;
}
