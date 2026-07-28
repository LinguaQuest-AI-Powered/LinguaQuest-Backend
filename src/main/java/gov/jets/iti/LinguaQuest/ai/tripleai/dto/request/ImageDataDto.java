package gov.jets.iti.LinguaQuest.ai.tripleai.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Immutable DTO representing an image format and base64 payload in Gateway multimodal requests.
 *
 * @param format     the image format (e.g. "png", "jpeg", "jpg", "webp")
 * @param dataBase64 the base64-encoded image data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ImageDataDto(
        @JsonProperty("format") String format,
        @JsonProperty("data_base64") String dataBase64
) {
}
