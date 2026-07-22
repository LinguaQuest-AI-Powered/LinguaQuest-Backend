package gov.jets.iti.LinguaQuest.util;

import gov.jets.iti.LinguaQuest.dto.RewardResult;
import gov.jets.iti.LinguaQuest.enums.Difficulty;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;

@Component
public class RewardCalculatorUtil {

    private static final int BASE_COINS_DIVIDER = 3;
    private static final Map<Difficulty, Integer> BASE_XP = Map.of(
            Difficulty.EASY, 20,
            Difficulty.MEDIUM, 35,
            Difficulty.HARD, 50
    );

    private static final Map<Difficulty, Double> WORLD_MULTIPLIER = Map.of(
            Difficulty.EASY, 1.0,
            Difficulty.MEDIUM, 1.25,
            Difficulty.HARD, 1.5
    );

    public RewardResult calculate(Difficulty wordDifficulty, Difficulty worldDifficulty) {
        int baseXp = BASE_XP.get(wordDifficulty);
        double multiplier = WORLD_MULTIPLIER.get(worldDifficulty);
        int xp = (int) Math.round(baseXp * multiplier);
        int coins = Math.max(1, xp / BASE_COINS_DIVIDER);
        return new RewardResult(xp, coins);
    }
}