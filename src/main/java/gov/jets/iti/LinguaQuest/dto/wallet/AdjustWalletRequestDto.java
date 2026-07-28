package gov.jets.iti.LinguaQuest.dto.wallet;

import jakarta.validation.constraints.NotNull;

public record AdjustWalletRequestDto(@NotNull(message = "xpDelta is required") Integer xpDelta,
                                     @NotNull(message = "coinsDelta is required") Integer coinsDelta) {
}
