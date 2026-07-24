package gov.jets.iti.LinguaQuest.controller;


import gov.jets.iti.LinguaQuest.dto.request.AdjustWalletRequestDto;
import gov.jets.iti.LinguaQuest.dto.response.AdjustWalletResponseDto;
import gov.jets.iti.LinguaQuest.dto.response.SuccessResponse;
import gov.jets.iti.LinguaQuest.dto.response.WalletResponseDto;
import gov.jets.iti.LinguaQuest.service.WalletService;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/{version}/wallet")
public class WalletController {

    private final WalletService walletService;

    @GetMapping(version = "v1")
    public ResponseEntity<SuccessResponse<WalletResponseDto>> getUserWalletStats(@AuthenticationPrincipal UserPrinciple userPrinciple) {
        WalletResponseDto walletResponseDto = walletService.getUserWalletStats(userPrinciple);
        return ResponseEntity.ok(new SuccessResponse<>(true,walletResponseDto));
    }

    @PostMapping(value = "/adjust",version = "v1")
    public ResponseEntity<SuccessResponse<AdjustWalletResponseDto>> adjustUserWallet(@AuthenticationPrincipal UserPrinciple userPrinciple,
                                                                                     @RequestBody @Valid AdjustWalletRequestDto adjustWalletRequestDto) {

        AdjustWalletResponseDto adjustWalletResponseDto = walletService.adjustUserWallet(userPrinciple,adjustWalletRequestDto.xpDelta(), adjustWalletRequestDto.coinsDelta());
        return ResponseEntity.ok(new SuccessResponse<>(true, adjustWalletResponseDto));
    }
}
