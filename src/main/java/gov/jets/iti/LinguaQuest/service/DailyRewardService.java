package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.dto.response.DailyRewardClaimResponse;
import gov.jets.iti.LinguaQuest.dto.response.DailyRewardStatusResponse;
import gov.jets.iti.LinguaQuest.entity.DailyRewardTier;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.entity.UserDailyRewardClaim;
import gov.jets.iti.LinguaQuest.exception.DailyRewardAlreadyClaimedException;
import gov.jets.iti.LinguaQuest.exception.auth.EmailNotFoundException;
import gov.jets.iti.LinguaQuest.repository.DailyRewardTierRepository;
import gov.jets.iti.LinguaQuest.repository.UserDailyRewardClaimRepository;
import gov.jets.iti.LinguaQuest.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DailyRewardService {

    private final DailyRewardTierRepository dailyRewardTierRepository;
    private final UserDailyRewardClaimRepository userDailyRewardClaimRepository;
    private final UserRepository userRepository;

    public DailyRewardStatusResponse getStatus(Long userId){

        User user = userRepository.findByIdWithNativeLanguage(userId)
                .orElseThrow(() -> new EmailNotFoundException("User not found"));
        LocalDate today = LocalDate.now();
        int cycleLength = getCycleLength();
        if(today.equals(user.getLastDailyRewardClaimedAt())){
            UserDailyRewardClaim todayClaim = userDailyRewardClaimRepository.findByUserAndClaimedDate(user, today)
                    .orElseThrow(() -> new IllegalStateException("Claim record missing for today"));

            return new DailyRewardStatusResponse(
                    true,
                    todayClaim.getCycleDay(),
                    cycleLength,
                    todayClaim.getCoinsAwarded(),
                    todayClaim.getXpAwarded()
            );
        }
        int effectiveDay = resolveEffectiveDay(user, today);
        DailyRewardTier dailyReward = getTier(effectiveDay);

        return new DailyRewardStatusResponse(
                false,
                effectiveDay,
                cycleLength,
                dailyReward.getRewardCoins(),
                dailyReward.getRewardXp()
        );
    }

    @Transactional
    public DailyRewardClaimResponse claim(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EmailNotFoundException("User not found"));
        LocalDate today = LocalDate.now();
        if (today.equals(user.getLastDailyRewardClaimedAt())) {
            throw new DailyRewardAlreadyClaimedException("You've already claimed today's reward");
        }
        int cycleLength = getCycleLength();
        int effectiveDay = resolveEffectiveDay(user, today);
        DailyRewardTier tier = getTier(effectiveDay);
        UserDailyRewardClaim claim = UserDailyRewardClaim.builder()
                .user(user)
                .claimedDate(today)
                .cycleDay(effectiveDay)
                .coinsAwarded(tier.getRewardCoins())
                .xpAwarded(tier.getRewardXp())
                .build();

        userDailyRewardClaimRepository.save(claim);
        user.setCoins(user.getCoins() + tier.getRewardCoins());
        user.setXp(user.getXp() + tier.getRewardXp());
        user.setCurrentRewardDay((effectiveDay % cycleLength) + 1);
        user.setLastDailyRewardClaimedAt(today);
        userRepository.save(user);

        return new DailyRewardClaimResponse(
                tier.getRewardCoins(),
                tier.getRewardXp(),
                user.getCoins(),
                user.getXp(),
                user.getCurrentRewardDay()
        );

    }

    private int getCycleLength() {
        return dailyRewardTierRepository.findMaxDayNumber()
                .orElseThrow(() -> new IllegalStateException("Daily reward tiers not configured"));
    }
    private int resolveEffectiveDay(User user, LocalDate today) {
        boolean continuingStreak = user.getLastDailyRewardClaimedAt() != null
                && user.getLastDailyRewardClaimedAt().equals(today.minusDays(1));
        return continuingStreak ? user.getCurrentRewardDay() : 1;
    }
    private DailyRewardTier getTier(int effectiveDay) {
        return dailyRewardTierRepository.findByDayNumber(effectiveDay)
                .orElseThrow(() -> new IllegalStateException("Daily reward tier not found"));
    }
}
