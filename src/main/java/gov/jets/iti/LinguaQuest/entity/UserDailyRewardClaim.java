package gov.jets.iti.LinguaQuest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_daily_reward_claims",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "claimed_date"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDailyRewardClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "claimed_date", nullable = false)
    private LocalDate claimedDate;

    @Column(name = "cycle_day", nullable = false)
    private Integer cycleDay;

    @Column(name = "coins_awarded", nullable = false)
    private Integer coinsAwarded;

    @Column(name = "xp_awarded")
    private Integer xpAwarded;

    @Column(name = "claimed_at", nullable = false)
    @Builder.Default
    private LocalDateTime claimedAt = LocalDateTime.now();
}
