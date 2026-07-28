package gov.jets.iti.LinguaQuest.ai.tripleai.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Configuration properties for University AI Gateway (TripleAI).
 * <p>
 * Loaded automatically from application properties with prefix {@code spring.ai.tripleai}.
 */
@Validated
@ConfigurationProperties(prefix = "spring.ai.tripleai")
public class TripleAiProperties {

    /**
     * Base URL for the University AI Gateway (e.g. https://gateway.example.com/api/v1).
     */
    @NotBlank(message = "spring.ai.tripleai.base-url must not be blank")
    private String baseUrl;

    /**
     * API Key for authenticating with the University AI Gateway.
     */
    @NotBlank(message = "spring.ai.tripleai.api-key must not be blank")
    private String apiKey;

    /**
     * Default model identifier for text chat (e.g. openai.gpt-oss-20b-1:0).
     */
    @NotBlank(message = "spring.ai.tripleai.default-model must not be blank")
    private String defaultModel = "openai.gpt-oss-20b-1:0";

    /**
     * Default model identifier for multimodal / vision chat (e.g. qwen.qwen3-vl-235b-a22b).
     */
    private String multimodalModel;

    /**
     * Request timeout duration.
     */
    @NotNull(message = "spring.ai.tripleai.timeout must not be null")
    private Duration timeout = Duration.ofSeconds(60);

    /**
     * Default maximum generation tokens.
     */
    @NotNull(message = "spring.ai.tripleai.default-max-tokens must not be null")
    @Min(value = 1, message = "defaultMaxTokens must be at least 1")
    private Integer defaultMaxTokens = 300;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    public void setDefaultModel(String defaultModel) {
        this.defaultModel = defaultModel;
    }

    public String getMultimodalModel() {
        return multimodalModel;
    }

    public void setMultimodalModel(String multimodalModel) {
        this.multimodalModel = multimodalModel;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public Integer getDefaultMaxTokens() {
        return defaultMaxTokens;
    }

    public void setDefaultMaxTokens(Integer defaultMaxTokens) {
        this.defaultMaxTokens = defaultMaxTokens;
    }
}
