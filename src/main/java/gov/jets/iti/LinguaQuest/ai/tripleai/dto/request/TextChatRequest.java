package gov.jets.iti.LinguaQuest.ai.tripleai.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Immutable DTO representing the request payload for POST /student/chat.
 *
 * @param modelId      the Gateway model identifier (e.g. "anthropic.claude-3-haiku-20240307-v1:0")
 * @param messages     the list of text conversation messages
 * @param systemPrompt optional top-level system prompt
 * @param maxTokens    optional maximum generation tokens limit
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TextChatRequest(
        @JsonProperty("model_id") String modelId,
        @JsonProperty("messages") List<ChatMessageDto> messages,
        @JsonProperty("system_prompt") String systemPrompt,
        @JsonProperty("max_tokens") Integer maxTokens
) {
}
