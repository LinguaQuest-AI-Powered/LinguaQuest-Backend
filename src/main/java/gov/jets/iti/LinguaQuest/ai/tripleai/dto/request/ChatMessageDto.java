package gov.jets.iti.LinguaQuest.ai.tripleai.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Immutable DTO representing a text message object in Gateway text chat requests.
 *
 * @param role    the message role (e.g. "user", "assistant", "system")
 * @param content the text content of the message
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatMessageDto(
        @JsonProperty("role") String role,
        @JsonProperty("content") String content
) {
}
