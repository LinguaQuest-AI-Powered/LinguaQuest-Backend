package gov.jets.iti.LinguaQuest.entity;

import gov.jets.iti.LinguaQuest.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Builder.Default
    private Integer xp = 0;

    @Column(length = 500)
    private String photo;

    @Column(name = "native_language", length = 50)
    private String nativeLanguage;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_target_languages",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "language_id")
    )
    @Builder.Default
    private Set<TargetLanguage> targetLanguages = new HashSet<>();

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer level = 1;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Column(name = "reset_token")
    private String resetToken;
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "reset_token_expires_at")
    private LocalDateTime resetTokenExpiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return email != null && email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}