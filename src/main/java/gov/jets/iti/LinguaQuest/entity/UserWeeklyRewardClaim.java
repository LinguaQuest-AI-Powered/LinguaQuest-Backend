package gov.jets.iti.LinguaQuest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * NEW ENTITY. One row per ISO week claimed - weekStartDate should be the
 * Monday (or whatever your week-start convention is) of the claimed week,
 * so the unique constraint cleanly enforces "one claim per week".
 */
@Entity
@Table(
        name = "user_weekly_reward_claims",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "week_start_date"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWeeklyRewardClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "xp_awarded", nullable = false)
    private Integer xpAwarded;

    @Column(name = "coins_awarded", nullable = false)
    private Integer coinsAwarded;

    @Column(name = "claimed_at", nullable = false)
    @Builder.Default
    private LocalDateTime claimedAt = LocalDateTime.now();
}
