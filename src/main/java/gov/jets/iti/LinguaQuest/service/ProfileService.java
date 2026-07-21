package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.dto.request.ChangePasswordRequest;
import gov.jets.iti.LinguaQuest.dto.request.DeleteProfileRequest;
import gov.jets.iti.LinguaQuest.dto.request.UpdateProfileRequest;
import gov.jets.iti.LinguaQuest.dto.response.ImageUploadResponseDTO;
import gov.jets.iti.LinguaQuest.dto.response.PhotoUploadResponseDto;
import gov.jets.iti.LinguaQuest.dto.response.ProfileResponseDto;
import gov.jets.iti.LinguaQuest.dto.response.ProfileResponseDto.CurrentJourneyDto;
import gov.jets.iti.LinguaQuest.dto.response.ProfileResponseDto.ProfileStatsDto;
import gov.jets.iti.LinguaQuest.dto.response.ProfileUpdateResponseDto;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.entity.UserLanguage;
import gov.jets.iti.LinguaQuest.enums.SignInProvider;
import gov.jets.iti.LinguaQuest.exception.auth.EmailNotFoundException;
import gov.jets.iti.LinguaQuest.exception.profile.InvalidPasswordException;
import gov.jets.iti.LinguaQuest.exception.profile.UsernameAlreadyExistsException;
import gov.jets.iti.LinguaQuest.repository.RefreshTokenRepository;
import gov.jets.iti.LinguaQuest.repository.UserLanguageRepository;
import gov.jets.iti.LinguaQuest.repository.UserLevelProgressRepository;
import gov.jets.iti.LinguaQuest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final UserLanguageRepository userLanguageRepository;
    private final UserLevelProgressRepository userLevelProgressRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ImageService imageService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public ProfileResponseDto getProfile(Long userId) {
        User user = userRepository.findByIdWithNativeLanguage(userId)
                .orElseThrow(() -> new EmailNotFoundException("User not found"));

        String nativeLanguage = user.getNativeLanguage() != null ? user.getNativeLanguage().getName() : null;
        int worldsCount = userLevelProgressRepository.countDistinctCompletedWorldsByUserId(userId);

        ProfileStatsDto stats = new ProfileStatsDto(
                user.getCoins(),
                user.getXp(),
                user.getCurrentStreakDays(),
                worldsCount
        );

        Optional<UserLanguage> activeLanguageOpt = userLanguageRepository.findActiveByUserIdWithLanguage(userId);
        CurrentJourneyDto currentJourney = activeLanguageOpt.map(ul -> new CurrentJourneyDto(
                ul.getLanguage().getId(),
                ul.getLanguage().getName(),
                ul.getLanguage().getCode(),
                ul.getLevel(),
                deriveJourneyLabel(ul.getLevel()),
                ul.getCurrentXp(),
                ul.getNextMilestoneXp()
        )).orElse(null);

        return new ProfileResponseDto(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                nativeLanguage,
                user.getPhoto(),
                user.getLevel(),
                stats,
                currentJourney
        );
    }


    // cur handling username only
    @Transactional
    public ProfileUpdateResponseDto updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EmailNotFoundException("User not found"));

        if (request != null && request.username() != null && !request.username().isBlank()) {
            String newUsername = request.username().trim();
            if (!newUsername.equals(user.getUsername())) {
                if (userRepository.existsByUsername(newUsername)) {
                    throw new UsernameAlreadyExistsException("This username is already taken");
                }
                user.setUsername(newUsername);
                userRepository.save(user);
            }
        }

        return new ProfileUpdateResponseDto(user.getId(), user.getUsername());
    }

    @Transactional
    public void deleteProfile(Long userId, DeleteProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EmailNotFoundException("User not found"));

        if (SignInProvider.LOCAL.equals(user.getSignInProvider())) {
            if (request == null || request.password() == null || request.password().isBlank() ||
                    !passwordEncoder.matches(request.password(), user.getPassword())) {
                throw new InvalidPasswordException("Incorrect password");
            }
        }

        user.setDeleted(true);
        userRepository.save(user);

        refreshTokenRepository.revokeAllActiveByUserId(userId);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EmailNotFoundException("User not found"));

        if (!SignInProvider.LOCAL.equals(user.getSignInProvider())) {
            throw new InvalidPasswordException("Password change is only available for local accounts");
        }

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Incorrect old password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public PhotoUploadResponseDto uploadPhoto(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EmailNotFoundException("User not found"));

        ImageUploadResponseDTO uploadResponse = imageService.uploadPhoto(file);
        
        if (user.getPhotoPublicId() != null) {
            try {
                imageService.deletePhoto(user.getPhotoPublicId());
            } catch (Exception e) {
                log.warn("Failed to delete old photo from Cloudinary for user {}: {}", userId, e.getMessage());
            }
        }

        String photoUrl = uploadResponse.url();
        user.setPhoto(photoUrl);
        user.setPhotoPublicId(uploadResponse.publicId());
        userRepository.save(user);

        return new PhotoUploadResponseDto(photoUrl);
    }

    private String deriveJourneyLabel(int level) {
        if (level <= 5) {
            return "Beginner Journey";
        } else if (level <= 15) {
            return "Intermediate Journey";
        } else {
            return "Advanced Journey";
        }
    }
}
