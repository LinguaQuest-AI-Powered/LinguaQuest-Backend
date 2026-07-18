package gov.jets.iti.LinguaQuest.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {
    private final SecretKey secretKey;
    private final Long expirationMs;
    public JwtUtil(Environment env) {
        String secretKey = env.getProperty(ApplicationConstants.JWT_SECRET_KEY,ApplicationConstants.JWT_DEFAULT_SECRET_KEY);
        SecretKey secretKey1 = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.secretKey = secretKey1;
        Long expirationMs = env.getProperty(ApplicationConstants.JWT_EXPIRATION, Long.class);
        this.expirationMs = expirationMs;
    }
    public String generateToken(UserPrinciple userPrinciple) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userPrinciple.getUsername())
                .claim("roles",
                        userPrinciple.getAuthorities()
                                .stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String email = extractEmail(token);
            System.out.println("Extracted email from token: " + email);
            return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long getExpirationMs() {
        return expirationMs;
    }
}
