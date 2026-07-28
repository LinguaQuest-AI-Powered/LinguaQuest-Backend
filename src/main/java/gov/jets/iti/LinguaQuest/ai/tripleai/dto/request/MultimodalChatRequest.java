package gov.jets.iti.LinguaQuest.ai.tripleai.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Immutable DTO representing the request payload for POST /student/multimodal-chat.
 *
 * @param modelId   the Gateway model identifier (e.g. "qwen.qwen3-vl-235b-a22b")
 * @param messages  the list of multimodal messages containing text and image payloads
 * @param maxTokens optional maximum generation tokens limit
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MultimodalChatRequest(
        @JsonProperty("model_id") String modelId,
        @JsonProperty("messages") List<MultimodalMessageDto> messages,
        @JsonProperty("max_tokens") Integer maxTokens
) {
}
