package gov.jets.iti.LinguaQuest.ai.tripleai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Immutable top-level container for Gateway error responses.
 *
 * @param error the inner GatewayError payload
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GatewayErrorContainer(
        @JsonProperty("error") GatewayError error
) {
}
