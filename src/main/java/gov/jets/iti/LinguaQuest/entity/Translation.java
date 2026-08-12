package gov.jets.iti.LinguaQuest.entity;

import gov.jets.iti.LinguaQuest.enums.TranslatableEntityType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "translations",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"entity_type", "entity_id", "language_id", "field_name"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Translation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private TranslatableEntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(name = "field_name", nullable = false, length = 50)
    private String fieldName;

    @Column(nullable = false, length = 500)
    private String value;
}