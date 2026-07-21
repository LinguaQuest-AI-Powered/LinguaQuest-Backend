package gov.jets.iti.LinguaQuest.entity;

import gov.jets.iti.LinguaQuest.enums.Role;
import gov.jets.iti.LinguaQuest.enums.SignInProvider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
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

    @Column(name = "firebase_uid", unique = true)
    private String firebaseUid;

    @Enumerated(EnumType.STRING)
    @Column(name = "sign_in_provider", nullable = false)
    @Builder.Default
    private SignInProvider signInProvider = SignInProvider.LOCAL;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Builder.Default
    private Integer coins = 0;


    @Column(nullable = false)
    @Builder.Default
    private Integer xp = 0;

    @Column(length = 500)
    private String photo;

    @Column(name = "photo_public_id", length = 200)
    private String photoPublicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "native_language_id")
    private Language nativeLanguage;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<UserLanguage> languages = new HashSet<>();

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "profile_complete", nullable = false)
    @Builder.Default
    private boolean profileComplete = false;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer level = 1;

    @Column(name = "current_streak_days", nullable = false)
    @Builder.Default
    private Integer currentStreakDays = 0;

    @Column(name = "current_reward_day", nullable = false)
    @Builder.Default
    private Integer currentRewardDay = 1;

    @Column(name = "last_daily_reward_claimed_at")
    private LocalDate lastDailyRewardClaimedAt;

    @Column(name = "last_weekly_reward_claimed_at")
    private LocalDate lastWeeklyRewardClaimedAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Enumerated(EnumType.STRING)
    private Role role;

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