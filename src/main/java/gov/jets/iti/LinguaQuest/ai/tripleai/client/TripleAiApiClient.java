package gov.jets.iti.LinguaQuest.ai.tripleai.client;

import gov.jets.iti.LinguaQuest.ai.tripleai.config.TripleAiProperties;
import gov.jets.iti.LinguaQuest.ai.tripleai.dto.request.MultimodalChatRequest;
import gov.jets.iti.LinguaQuest.ai.tripleai.dto.request.TextChatRequest;
import gov.jets.iti.LinguaQuest.ai.tripleai.dto.response.GatewayChatResponse;
import gov.jets.iti.LinguaQuest.ai.tripleai.dto.response.GatewayError;
import gov.jets.iti.LinguaQuest.ai.tripleai.dto.response.GatewayErrorContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Objects;

/**
 * Isolated REST API client for interacting with the University AI Gateway endpoints.
 */
public class TripleAiApiClient {

    private static final Logger log = LoggerFactory.getLogger(TripleAiApiClient.class);

    private static final String TEXT_CHAT_ENDPOINT = "/student/chat";
    private static final String MULTIMODAL_CHAT_ENDPOINT = "/student/multimodal-chat";

    private final RestClient restClient;

    public TripleAiApiClient(TripleAiProperties properties) {
        Objects.requireNonNull(properties, "TripleAiProperties must not be null");

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        if (properties.getTimeout() != null) {
            int timeoutMillis = (int) properties.getTimeout().toMillis();
            requestFactory.setConnectTimeout(timeoutMillis);
            requestFactory.setReadTimeout(timeoutMillis);
        }

        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(requestFactory)
                .build();
    }

    public TripleAiApiClient(RestClient restClient) {
        this.restClient = Objects.requireNonNull(restClient, "RestClient must not be null");
    }

    /**
     * Sends a text chat request to POST /student/chat.
     *
     * @param request the text chat request payload
     * @return the gateway chat response
     */
    public GatewayChatResponse sendTextChat(TextChatRequest request) {
        log.info("--> [TripleAI Request] Text Chat | Model: {} | Messages: {} | MaxTokens: {}",
                request.modelId(),
                request.messages() != null ? request.messages().size() : 0,
                request.maxTokens());
        try {
            GatewayChatResponse response = restClient.post()
                    .uri(TEXT_CHAT_ENDPOINT)
                    .body(request)
                    .retrieve()
                    .body(GatewayChatResponse.class);
            logResponse("Text Chat", response);
            return response;
        } catch (RestClientResponseException e) {
            throw handleRestClientException("TextChat", e);
        } catch (Exception e) {
            log.error("Failed to execute text chat request to Gateway", e);
            throw new TripleAiException("Failed to communicate with University AI Gateway: " + e.getMessage(), e);
        }
    }

    /**
     * Sends a multimodal chat request to POST /student/multimodal-chat.
     *
     * @param request the multimodal chat request payload
     * @return the gateway chat response
     */
    public GatewayChatResponse sendMultimodalChat(MultimodalChatRequest request) {
        int totalImages = 0;
        if (request.messages() != null) {
            for (var msg : request.messages()) {
                if (msg.images() != null) {
                    totalImages += msg.images().size();
                }
            }
        }
        log.info("--> [TripleAI Request] Multimodal Chat | Model: {} | Messages: {} | Images Attached: {} | MaxTokens: {}",
                request.modelId(),
                request.messages() != null ? request.messages().size() : 0,
                totalImages,
                request.maxTokens());
        try {
            GatewayChatResponse response = restClient.post()
                    .uri(MULTIMODAL_CHAT_ENDPOINT)
                    .body(request)
                    .retrieve()
                    .body(GatewayChatResponse.class);
            logResponse("Multimodal Chat", response);
            return response;
        } catch (RestClientResponseException e) {
            throw handleRestClientException("MultimodalChat", e);
        } catch (Exception e) {
            log.error("Failed to execute multimodal chat request to Gateway", e);
            throw new TripleAiException("Failed to communicate with University AI Gateway: " + e.getMessage(), e);
        }
    }

    private void logResponse(String requestType, GatewayChatResponse response) {
        if (response == null) {
            log.info("<-- [TripleAI Response] {} | Received null response body", requestType);
            return;
        }
        int inputTokens = response.usage() != null && response.usage().inputTokens() != null ? response.usage().inputTokens() : 0;
        int outputTokens = response.usage() != null && response.usage().outputTokens() != null ? response.usage().outputTokens() : 0;
        int totalTokens = response.usage() != null && response.usage().totalTokens() != null ? response.usage().totalTokens() : inputTokens + outputTokens;

        log.info("<-- [TripleAI Response] {} | RequestId: {} | Model: {} | Tokens: [in: {}, out: {}, total: {}] | Status: {}",
                requestType,
                response.requestId(),
                response.modelId(),
                inputTokens,
                outputTokens,
                totalTokens,
                response.status());

        if (log.isDebugEnabled() && response.outputText() != null) {
            String preview = response.outputText().length() > 100
                    ? response.outputText().substring(0, 100) + "..."
                    : response.outputText();
            log.debug("    Output Preview: {}", preview);
        }
    }

    private TripleAiException handleRestClientException(String requestType, RestClientResponseException e) {
        log.warn("Gateway returned HTTP {} for {} request", e.getStatusCode(), requestType);
        try {
            GatewayErrorContainer errorContainer = e.getResponseBodyAs(GatewayErrorContainer.class);
            if (errorContainer != null && errorContainer.error() != null) {
                GatewayError error = errorContainer.error();
                log.error("Gateway error response: code={}, message={}", error.code(), error.message());
                return new TripleAiException(error);
            }
        } catch (Exception parseException) {
            log.debug("Could not parse Gateway error body as GatewayErrorContainer", parseException);
        }
        return new TripleAiException(String.format("Gateway HTTP %s failure: %s", e.getStatusCode(), e.getStatusText()), e);
    }
}
