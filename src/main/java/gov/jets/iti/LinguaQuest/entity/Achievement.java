package gov.jets.iti.LinguaQuest.entity;

import gov.jets.iti.LinguaQuest.enums.AchievementTrigger;
import gov.jets.iti.LinguaQuest.enums.CriteriaType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "achievements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "criteria_type", nullable = false, length = 50)
    private CriteriaType criteriaType;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_event", nullable = false, length = 50)
    private AchievementTrigger triggerEvent;

    @Column(name = "target_value", nullable = false)
    private Integer targetValue;

    @Column(name = "target_world_id")
    private Long targetWorldId;

    @Column(name = "xp_reward")
    private Integer xpReward;

    @Column(name = "coin_reward")
    private Integer coinReward;
}