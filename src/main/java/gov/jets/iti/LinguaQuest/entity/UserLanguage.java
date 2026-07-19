package gov.jets.iti.LinguaQuest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "user_languages",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "language_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLanguage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(nullable = false)
    @Builder.Default
    private Integer level = 1;

    @Column(name = "current_xp", nullable = false)
    @Builder.Default
    private Integer currentXp = 0;

    @Column(name = "next_milestone_xp", nullable = false)
    @Builder.Default
    private Integer nextMilestoneXp = 1000;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = false;

    @Column(name = "progress_percent", nullable = false)
    @Builder.Default
    private Integer progressPercent = 0;

    @Column(name = "levels_completed", nullable = false)
    @Builder.Default
    private Integer levelsCompleted = 0;

    @Column(name = "words_learned", nullable = false)
    @Builder.Default
    private Integer wordsLearned = 0;

    @Column(name = "added_at", nullable = false)
    @Builder.Default
    private LocalDateTime addedAt = LocalDateTime.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserLanguage)) return false;
        UserLanguage that = (UserLanguage) o;
        return user != null && language != null
                && user.getId() != null && user.getId().equals(that.user.getId())
                && language.getId() != null && language.getId().equals(that.language.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(user != null ? user.getId() : null,
                language != null ? language.getId() : null);
    }
}