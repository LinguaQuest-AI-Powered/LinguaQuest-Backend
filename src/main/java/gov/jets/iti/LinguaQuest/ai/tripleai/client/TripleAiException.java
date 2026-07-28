package gov.jets.iti.LinguaQuest.ai.tripleai.client;

import gov.jets.iti.LinguaQuest.ai.tripleai.dto.response.GatewayError;

import java.util.Collections;
import java.util.Map;

/**
 * Custom runtime exception thrown when the University AI Gateway returns an error or fails.
 */
public class TripleAiException extends RuntimeException {

    private final String errorCode;
    private final Map<String, Object> details;

    public TripleAiException(String message) {
        super(message);
        this.errorCode = "UNKNOWN_ERROR";
        this.details = Collections.emptyMap();
    }

    public TripleAiException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "CLIENT_ERROR";
        this.details = Collections.emptyMap();
    }

    public TripleAiException(GatewayError gatewayError) {
        super(formatMessage(gatewayError));
        this.errorCode = gatewayError != null && gatewayError.code() != null ? gatewayError.code() : "UNKNOWN_ERROR";
        this.details = gatewayError != null && gatewayError.details() != null ? gatewayError.details() : Collections.emptyMap();
    }

    private static String formatMessage(GatewayError gatewayError) {
        if (gatewayError == null) {
            return "Gateway error occurred with no response details.";
        }
        return String.format("[%s] %s",
                gatewayError.code() != null ? gatewayError.code() : "UNKNOWN",
                gatewayError.message() != null ? gatewayError.message() : "No message provided");
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
