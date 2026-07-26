package gov.jets.iti.LinguaQuest.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "word_hints",
        uniqueConstraints = @UniqueConstraint(columnNames = {"word_code", "language_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class WordHint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "word_code", nullable = false, length = 120)
    String wordCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(name = "hint_text", nullable = false, length = 500)
    String hintText;
}
