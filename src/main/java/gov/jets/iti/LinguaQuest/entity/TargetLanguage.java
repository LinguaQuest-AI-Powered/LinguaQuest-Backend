package gov.jets.iti.LinguaQuest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "target_languages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TargetLanguage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(unique = true, length = 5)
    private String code;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TargetLanguage)) return false;
        TargetLanguage language = (TargetLanguage) o;
        return name != null && name.equals(language.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}