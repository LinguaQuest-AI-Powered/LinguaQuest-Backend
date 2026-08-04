package gov.jets.iti.LinguaQuest.entity;

import gov.jets.iti.LinguaQuest.enums.DevicePlatform;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "device_tokens",
        uniqueConstraints = @UniqueConstraint(columnNames = {"token"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DevicePlatform platform;

    @Column(name = "last_used_at", nullable = false)
    @Builder.Default
    private LocalDateTime lastUsedAt = LocalDateTime.now();
}