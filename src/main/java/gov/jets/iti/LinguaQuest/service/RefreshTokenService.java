package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.entity.RefreshToken;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.exception.auth.InvalidRefreshTokenException;
import gov.jets.iti.LinguaQuest.exception.auth.RefreshTokenExpiredException;
import gov.jets.iti.LinguaQuest.repository.RefreshTokenRepository;
import gov.jets.iti.LinguaQuest.util.ApplicationConstants;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final Long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, Environment env) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpirationMs = env.getProperty(
                ApplicationConstants.REFRESH_TOKEN_EXPIRATION,
                Long.class,
                604800000L // Default to 7 days
        );
    }

    /**
     * Generates a new secure random refresh token and saves its SHA-256 hash in the database.
     * Returns the raw token.
     */
    public String createRefreshToken(User user) {
        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    public record TokenRotationResult(String newRawRefreshToken, User user) {}

    /**
     * Validates the raw refresh token, revokes it, performs rotation (RTR),
     * and returns a newly generated raw refresh token.
     */
    public TokenRotationResult validateAndRotate(String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        if (token.isRevoked()) {

            refreshTokenRepository.revokeAllActiveByUserId(token.getUser().getId());
            throw new InvalidRefreshTokenException("Refresh token has been reused. All active sessions revoked for security.");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RefreshTokenExpiredException("Refresh token has expired");
        }

        // Revoke the current token
        token.setRevoked(true);
        token.setLastUsedAt(LocalDateTime.now());
        refreshTokenRepository.save(token);

        // Generate a new one
        String newRawRefreshToken = createRefreshToken(token.getUser());
        return new TokenRotationResult(newRawRefreshToken, token.getUser());
    }

    /**
     * Background task to purge expired or revoked refresh tokens.
     * Runs every day at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void purgeExpiredOrRevokedTokens() {
        refreshTokenRepository.deleteExpiredOrRevoked(LocalDateTime.now());
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Hex.encodeHexString(bytes);
    }

    private String hashToken(String rawToken) {
        return DigestUtils.sha256Hex(rawToken);
    }
}
