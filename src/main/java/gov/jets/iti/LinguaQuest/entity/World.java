package gov.jets.iti.LinguaQuest.entity;

import gov.jets.iti.LinguaQuest.enums.Difficulty;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "worlds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class World {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @OneToMany(mappedBy = "world", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorldLevel> worldLevels = new ArrayList<>();

    @ManyToMany(mappedBy = "worlds", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Word> words = new HashSet<>();
}