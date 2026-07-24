package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.dto.response.AdjustWalletResponseDto;
import gov.jets.iti.LinguaQuest.dto.response.WalletResponseDto;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.exception.InvalidOperationException;
import gov.jets.iti.LinguaQuest.repository.UserRepository;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.intellij.lang.annotations.PrintFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletService {
    final private UserRepository userRepository;
    public WalletResponseDto getUserWalletStats(UserPrinciple userPrinciple) {
        return new WalletResponseDto(userPrinciple.user().getXp(), userPrinciple.user().getCoins());
    }

    @Transactional
    public AdjustWalletResponseDto adjustUserWallet(@AuthenticationPrincipal UserPrinciple userPrinciple, Integer xpDelta, Integer coinsDelta) {

        User user = userRepository.findById(userPrinciple.user().getId()).get();
        if((user.getCoins() + coinsDelta < 0) || (user.getXp() + xpDelta < 0)) {
            throw new InvalidOperationException("Can't update the xp and coins. their value mustn't be negative");
        }
        user.setCoins(user.getCoins() + coinsDelta);
        user.setXp(user.getXp() + xpDelta);
        userRepository.save(user);

        return new AdjustWalletResponseDto(xpDelta,coinsDelta, user.getXp(),user.getCoins());
    }
}
