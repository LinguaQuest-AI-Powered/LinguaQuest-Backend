package gov.jets.iti.LinguaQuest.entity;

import gov.jets.iti.LinguaQuest.enums.Difficulty;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "words")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(nullable = false, length = 100)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "word_worlds",
            joinColumns = @JoinColumn(name = "word_id"),
            inverseJoinColumns = @JoinColumn(name = "world_id")
    )
    @Builder.Default
    private Set<World> worlds = new HashSet<>();
}