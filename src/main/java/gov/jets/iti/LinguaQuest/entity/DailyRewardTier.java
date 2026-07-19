package gov.jets.iti.LinguaQuest.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "daily_reward_tiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyRewardTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_number", nullable = false, unique = true)
    private Integer dayNumber;

    @Column(name = "reward_coins", nullable = false)
    private Integer rewardCoins;

    @Column(name = "reward_xp")
    private Integer rewardXp;
}
