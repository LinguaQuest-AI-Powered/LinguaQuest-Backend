package gov.jets.iti.LinguaQuest.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * NEW ENTITY. Catalog data - "Trophies" - seeded by admins. AchievementSummary
 * exposes status/progressPercent, which are per-user and live on
 * UserAchievement instead.
 */
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

    @Column(name = "criteria_type", length = 50)
    private String criteriaType;

    @Column(name = "target_value")
    private Integer targetValue;
}
