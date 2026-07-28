package gov.jets.iti.LinguaQuest.ai.tripleai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Immutable DTO representing structured error information from the Gateway.
 *
 * @param code    error code (e.g. "AUTH_INVALID", "MODEL_NOT_ALLOWED", "BEDROCK_ERROR")
 * @param message human-readable error description
 * @param details optional additional error detail map
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GatewayError(
        @JsonProperty("code") String code,
        @JsonProperty("message") String message,
        @JsonProperty("details") Map<String, Object> details
) {
}
