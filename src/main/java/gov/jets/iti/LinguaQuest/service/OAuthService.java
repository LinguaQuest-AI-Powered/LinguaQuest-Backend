package gov.jets.iti.LinguaQuest.service;

import com.google.firebase.auth.FirebaseToken;
import gov.jets.iti.LinguaQuest.entity.Language;
import gov.jets.iti.LinguaQuest.enums.Role;
import gov.jets.iti.LinguaQuest.dto.response.OAuthResponseDto;
import gov.jets.iti.LinguaQuest.dto.response.UserDto;
import gov.jets.iti.LinguaQuest.enums.SignInProvider;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.exception.auth.EmailAlreadyExistsException;
import gov.jets.iti.LinguaQuest.repository.UserLanguageRepository;
import gov.jets.iti.LinguaQuest.repository.UserRepository;
import gov.jets.iti.LinguaQuest.util.JwtUtil;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final FirebaseTokenVerifierService firebaseTokenVerifierService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final UserLanguageRepository userLanguageRepository;

    @Transactional
    public OAuthResponseDto firebaseLogin(String idToken) {
        FirebaseToken firebaseToken = firebaseTokenVerifierService.verifyIdToken(idToken);

        String uid = firebaseToken.getUid();
        String email = firebaseToken.getEmail();
        SignInProvider provider = resolveProvider(firebaseToken);

        // 1. Returning user — match by Firebase UID
        Optional<User> existingByUid = userRepository.findByFirebaseUid(uid);
        if (existingByUid.isPresent()) {
            User user = existingByUid.get();
            return buildResponse(user);
        }

        // 2. Email collision — reject if a LOCAL account already has this email
        Optional<User> existingByEmail = userRepository.findUserByEmail(email);
        if (existingByEmail.isPresent()) {
            throw new EmailAlreadyExistsException(
                    "An account with this email already exists. Please log in with your email and password.");
        }

        // 3. New user — create account
        User newUser = createOAuthUser(email, uid, provider);
        return buildResponse(newUser);
    }

    private User createOAuthUser(String email, String firebaseUid, SignInProvider provider) {
        String username = generateUsername(email);
        String sentinelPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        User user = User.builder()
                .email(email)
                .firebaseUid(firebaseUid)
                .signInProvider(provider)
                .username(username)
                .password(sentinelPassword)
                .isVerified(true)
                .role(Role.ROLE_USER)
                .profileComplete(false)
                .build();

        return userRepository.save(user);
    }

    private String generateUsername(String email) {
        String prefix = email.substring(0, email.indexOf('@'));
        if (!userRepository.existsByUsername(prefix)) {
            return prefix;
        }
        // Handle collision by appending random suffix
        String candidate;
        do {
            candidate = prefix + "_" + UUID.randomUUID().toString().substring(0, 5);
        } while (userRepository.existsByUsername(candidate));
        return candidate;
    }

    private OAuthResponseDto buildResponse(User user) {
        UserPrinciple userPrinciple = new UserPrinciple(user);
        String accessToken = jwtUtil.generateToken(userPrinciple);
        String refreshToken = refreshTokenService.createRefreshToken(user);
        Long expiry = jwtUtil.getExpirationMs();

        boolean profileComplete = user.isProfileComplete();
        Set<Language> targetLanguage = userLanguageRepository.findLanguageByUserId(user.getId());
        UserDto userDto = new UserDto(
                user.getId(),
                user.getUsername(),
                user.getPhoto(),
                user.getNativeLanguage().getName(),
                user.getIsVerified(),
                targetLanguage
        );

        return new OAuthResponseDto(
                accessToken,
                refreshToken,
                "Bearer",
                expiry,
                profileComplete,
                userDto
        );
    }

    @SuppressWarnings("unchecked")
    private SignInProvider resolveProvider(FirebaseToken token) {
        Object firebaseClaim = token.getClaims().get("firebase");
        if (firebaseClaim instanceof java.util.Map) {
            Object signInProvider = ((java.util.Map<String, Object>) firebaseClaim).get("sign_in_provider");
            if (signInProvider instanceof String provider) {
                if (provider.contains("apple")) {
                    return SignInProvider.APPLE;
                }
            }
        }
        return SignInProvider.GOOGLE;
    }
}
