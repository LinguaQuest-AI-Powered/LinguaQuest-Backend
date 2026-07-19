package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.enums.Role;
import gov.jets.iti.LinguaQuest.dto.request.LoginRequestDto;
import gov.jets.iti.LinguaQuest.dto.request.RegisterRequestDto;
import gov.jets.iti.LinguaQuest.dto.request.*;
import gov.jets.iti.LinguaQuest.dto.response.AuthResponseDto;
import gov.jets.iti.LinguaQuest.dto.response.PasswordResetOtpVerifyResponse;
import gov.jets.iti.LinguaQuest.dto.response.RegisterResponseDto;
import gov.jets.iti.LinguaQuest.dto.response.UserDto;
import gov.jets.iti.LinguaQuest.dto.response.RefreshTokenResponseDto;
import gov.jets.iti.LinguaQuest.dto.request.RefreshTokenRequestDto;
import gov.jets.iti.LinguaQuest.enums.SignInProvider;
import gov.jets.iti.LinguaQuest.entity.Language;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.exception.auth.*;
import gov.jets.iti.LinguaQuest.repository.LanguageRepository;
import gov.jets.iti.LinguaQuest.repository.UserRepository;
import gov.jets.iti.LinguaQuest.util.JwtUtil;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import jakarta.transaction.Transactional;
import gov.jets.iti.LinguaQuest.enums.OtpPurpose;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.codec.digest.DigestUtils;


@Service
@RequiredArgsConstructor
public class AuthService {
    final private AuthenticationManager authenticationManager;
    final private JwtUtil jwtUtil;
    final private UserRepository userRepository;
    final private LanguageRepository targetLanguageRepository;
    final private PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final OtpService otpService;
    private final StringRedisTemplate stringRedisTemplate;
    private final RefreshTokenService refreshTokenService;

    private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(15);
    private static final String RESET_TOKEN_PREFIX = "reset-token:";

    @Transactional
    public RegisterResponseDto register(RegisterRequestDto registerRequestDto) {
        if (userRepository.existsByEmail(registerRequestDto.email())) {
            throw new EmailAlreadyExistsException("Email " + registerRequestDto.email() + " already exists");
        }
        if (userRepository.existsByUsername(registerRequestDto.username())) {
            throw new UsernameAlreadyExistsException("username " + registerRequestDto.username() + " already exists");
        }
        Language targetLanguage = targetLanguageRepository.findByName(registerRequestDto.targetLanguage());
        if (targetLanguage == null) {
            throw new TargetLanguageNotSupportedException("Language " + registerRequestDto.targetLanguage() + "is Not supported");
        }
        Set<Language> targetLanguageSet = new HashSet<>();
        targetLanguageSet.add(targetLanguage);

        User user = User.builder()
                .username(registerRequestDto.username())
                .email(registerRequestDto.email())
                .isVerified(false)
                .password(passwordEncoder.encode(registerRequestDto.password()))
                .nativeLanguage(registerRequestDto.nativeLanguage())
                .targetLanguages(targetLanguageSet)
                .role(Role.ROLE_USER)
                .signInProvider(SignInProvider.LOCAL).profileComplete(true)
                .build();
        userRepository.save(user);
        return mapUserToRegisterResponseDto(user);
    }


    public AuthResponseDto login(LoginRequestDto loginRequestDto) {

        var resultAuth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestDto.email(), loginRequestDto.password()));

        UserPrinciple userPrinciple = (UserPrinciple) resultAuth.getPrincipal();
        if (!userPrinciple.user().getIsVerified()) {
            throw new EmailNotVerifiedException("Please verify your email before logging in");
        }
        String jwtToken = jwtUtil.generateToken(userPrinciple);
        String refreshToken = refreshTokenService.createRefreshToken(userPrinciple.user());
        UserDto userDto = mapUserPrincipleToUserDto(userPrinciple);
        return new AuthResponseDto(jwtToken, refreshToken, "Bearer", jwtUtil.getExpirationMs(), userDto);

    }

    public RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto request) {
        RefreshTokenService.TokenRotationResult rotationResult = refreshTokenService.validateAndRotate(request.refreshToken());
        UserPrinciple userPrinciple = new UserPrinciple(rotationResult.user());
        String newAccessToken = jwtUtil.generateToken(userPrinciple);
        return new RefreshTokenResponseDto(
                newAccessToken,
                rotationResult.newRawRefreshToken(),
                "Bearer",
                jwtUtil.getExpirationMs()
        );
    }

    private UserDto mapUserPrincipleToUserDto(UserPrinciple userPrinciple) {
        return new UserDto(userPrinciple.user().getId(), userPrinciple.user().getUsername(),userPrinciple.user().getPhoto()
        ,userPrinciple.user().getNativeLanguage(),userPrinciple.user().getIsVerified(),userPrinciple.user().getTargetLanguages());
    }

    private RegisterResponseDto mapUserToRegisterResponseDto(User user){
        return new RegisterResponseDto(user.getId(), user.getEmail(),user.getUsername(),
                user.getNativeLanguage(),user.getTargetLanguages().stream().toList().get(0).getName(),
                user.getIsVerified());
    }
    public void sendOtp(OtpSendRequest request) {
        validateEmailExists(request.email());
        otpService.generateAndSendOtp(request.email(), request.purpose());
    }

    public void verifySignupOtp(OtpVerifyRequest request) {
        validateEmailExists(request.email());
        otpService.verifyOtp(request.email(), OtpPurpose.SIGNUP, request.otp());
        userService.markEmailVerified(request.email());
    }

    public PasswordResetOtpVerifyResponse verifyPasswordResetOtp(OtpVerifyRequest request) {
        validateEmailExists(request.email());
        otpService.verifyOtp(request.email(), OtpPurpose.PASSWORD_RESET, request.otp());

        String rawToken = generateResetToken();
        String hashedToken = DigestUtils.sha256Hex(rawToken);

        stringRedisTemplate.opsForValue().set(
                RESET_TOKEN_PREFIX + hashedToken,
                request.email(),
                RESET_TOKEN_TTL
        );

        return new PasswordResetOtpVerifyResponse(rawToken, RESET_TOKEN_TTL.toSeconds());
    }

    public void setNewPassword(ForgetPasswordRequest request) {
        String hashedToken = DigestUtils.sha256Hex(request.resetToken());
        String key = RESET_TOKEN_PREFIX + hashedToken;

        String email = stringRedisTemplate.opsForValue().get(key);
        if (email == null) {
            throw new InvalidResetTokenException("Reset token is invalid or has already been used");
        }

        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new EmailNotFoundException("Email not found"));
        userService.updatePassword(user, passwordEncoder.encode(request.newPassword()));

        stringRedisTemplate.delete(key);
    }

    private void validateEmailExists(String email) {
        if (!userService.existsByEmail(email)) {
            throw new EmailNotFoundException("Email not found");
        }
    }


    private String generateResetToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return "rst_" + Hex.encodeHexString(bytes);
    }
}
