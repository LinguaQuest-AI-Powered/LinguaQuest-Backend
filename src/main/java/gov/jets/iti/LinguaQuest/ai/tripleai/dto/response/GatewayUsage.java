package gov.jets.iti.LinguaQuest.ai.tripleai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Immutable DTO representing token consumption and execution usage metadata.
 *
 * @param inputTokens  number of prompt/input tokens consumed
 * @param outputTokens number of completion/output tokens generated
 * @param totalTokens  total tokens consumed
 * @param stopReason   reason execution ended (e.g. "end_turn")
 * @param budgetState  state of user budget (e.g. "ok")
 * @param fallbackUsed whether a fallback model was invoked
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GatewayUsage(
        @JsonProperty("input_tokens") Integer inputTokens,
        @JsonProperty("output_tokens") Integer outputTokens,
        @JsonProperty("total_tokens") Integer totalTokens,
        @JsonProperty("stop_reason") String stopReason,
        @JsonProperty("budget_state") String budgetState,
        @JsonProperty("fallback_used") Boolean fallbackUsed
) {
}
