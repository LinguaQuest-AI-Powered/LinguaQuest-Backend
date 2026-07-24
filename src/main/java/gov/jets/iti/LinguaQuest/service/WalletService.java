package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.dto.response.WalletResponseDto;
import gov.jets.iti.LinguaQuest.repository.UserRepository;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletService {
    public WalletResponseDto getUserWalletStats(UserPrinciple userPrinciple) {
        return new WalletResponseDto(userPrinciple.user().getXp(), userPrinciple.user().getCoins());
    }
}
