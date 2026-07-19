package gov.jets.iti.LinguaQuest.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "world_levels",
        uniqueConstraints = @UniqueConstraint(columnNames = {"world_id", "order_index"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorldLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "world_id", nullable = false)
    private World world;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
}