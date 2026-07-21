package gov.jets.iti.LinguaQuest.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

// Catalog / reference table - one row per supported language (Spanish,
// French, Japanese...). Seeded by admins, not created by users.
@Entity
@Table(name = "languages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Language {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    @JsonValue
    private String name;

    @Column(unique = true, length = 5)
    private String code;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Language)) return false;
        Language language = (Language) o;
        return name != null && name.equals(language.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}