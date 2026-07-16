package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.dto.request.LoginRequestDto;
import gov.jets.iti.LinguaQuest.dto.request.RegisterRequestDto;
import gov.jets.iti.LinguaQuest.dto.response.AuthResponseDto;
import gov.jets.iti.LinguaQuest.dto.response.RegisterResponseDto;
import gov.jets.iti.LinguaQuest.dto.response.UserDto;
import gov.jets.iti.LinguaQuest.entity.SignInProvider;
import gov.jets.iti.LinguaQuest.entity.TargetLanguage;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.exception.auth.EmailAlreadyExistsException;
import gov.jets.iti.LinguaQuest.exception.auth.EmailNotVerifiedException;
import gov.jets.iti.LinguaQuest.exception.auth.TargetLanguageNotSupportedException;
import gov.jets.iti.LinguaQuest.exception.auth.UsernameAlreadyExistsException;
import gov.jets.iti.LinguaQuest.repository.TargetLanguageRepository;
import gov.jets.iti.LinguaQuest.repository.UserRepository;
import gov.jets.iti.LinguaQuest.util.ApplicationConstants;
import gov.jets.iti.LinguaQuest.util.JwtUtil;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import jakarta.transaction.Transactional;
import gov.jets.iti.LinguaQuest.dto.request.OtpSendRequest;
import gov.jets.iti.LinguaQuest.dto.request.OtpVerifyRequest;
import gov.jets.iti.LinguaQuest.entity.OtpPurpose;
import gov.jets.iti.LinguaQuest.exception.auth.EmailNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    final private AuthenticationManager authenticationManager;
    final private JwtUtil jwtUtil;
    final private UserRepository userRepository;
    final private TargetLanguageRepository targetLanguageRepository;
    final private PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final OtpService otpService;
    @Transactional
    public RegisterResponseDto register(RegisterRequestDto registerRequestDto){
        if(userRepository.existsByEmail(registerRequestDto.email())) {
            throw new EmailAlreadyExistsException("Email " + registerRequestDto.email() + " already exists");
        }
        if(userRepository.existsByUsername(registerRequestDto.username())) {
            throw new UsernameAlreadyExistsException("username " + registerRequestDto.username() + " already exists");
        }
        TargetLanguage targetLanguage = targetLanguageRepository.findByName(registerRequestDto.targetLanguage());
        if(targetLanguage == null) {
            throw new TargetLanguageNotSupportedException("Language " + registerRequestDto.targetLanguage() + "is Not supported");
        }
        Set<TargetLanguage> targetLanguageSet = new HashSet<>();
        targetLanguageSet.add(targetLanguage);
        User user = User.builder()
                .username(registerRequestDto.username())
                .email(registerRequestDto.email())
                .isVerified(false)
                .signInProvider(SignInProvider.LOCAL)
                .profileComplete(true)
                .password(passwordEncoder.encode(registerRequestDto.password()))
                .nativeLanguage(registerRequestDto.nativeLanguage())
                .targetLanguages(targetLanguageSet)
                .build();
        userRepository.save(user);
        return mapUserToRegisterResponseDto(user);
    }


    public AuthResponseDto login(LoginRequestDto loginRequestDto) {

        var resultAuth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestDto.email(),loginRequestDto.password()));

        UserPrinciple userPrinciple = (UserPrinciple) resultAuth.getPrincipal();
        if(!userPrinciple.user().getIsVerified()) {
            throw new EmailNotVerifiedException("Please verify your email before logging in");
        }
        String jwtToken = jwtUtil.generateToken(userPrinciple);
        UserDto userDto = mapUserPrincipleToUserDto(userPrinciple);
        return new AuthResponseDto(jwtToken,"","Barear", 86400000L,userDto);

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

        otpService.generateAndSendOtp(
                request.email(),
                request.purpose()
        );
    }

    public void verifyOtp(OtpVerifyRequest request) {
        validateEmailExists(request.email());

        otpService.verifyOtp(
                request.email(),
                OtpPurpose.SIGNUP,
                request.otp()
        );

        userService.markEmailVerified(request.email());
    }

    private void validateEmailExists(String email) {
        if (!userService.existsByEmail(email)) {
            throw new EmailNotFoundException("Email not found");
        }
    }
}
