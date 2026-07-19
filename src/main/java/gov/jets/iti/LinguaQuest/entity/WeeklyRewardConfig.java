package gov.jets.iti.LinguaQuest.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "weekly_reward_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyRewardConfig {

    @Id
    private Long id;

    @Column(name = "reward_xp", nullable = false)
    private Integer rewardXp;

    @Column(name = "reward_coins", nullable = false)
    private Integer rewardCoins;
}
