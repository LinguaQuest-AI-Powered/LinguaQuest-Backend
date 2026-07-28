package gov.jets.iti.LinguaQuest.ai.tripleai.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Immutable DTO representing a multimodal message object in Gateway multimodal chat requests.
 *
 * @param role   the message role (e.g. "user", "assistant")
 * @param text   the text content of the message
 * @param images the list of attached images
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MultimodalMessageDto(
        @JsonProperty("role") String role,
        @JsonProperty("text") String text,
        @JsonProperty("images") List<ImageDataDto> images
) {
}
