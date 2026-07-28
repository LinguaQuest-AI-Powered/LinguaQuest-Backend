package gov.jets.iti.LinguaQuest.ai.tripleai.model;

import org.springframework.ai.chat.prompt.ChatOptions;

import java.util.List;
import java.util.Objects;

/**
 * Chat options implementation for the University AI Gateway (TripleAI).
 */
public class TripleAiChatOptions implements ChatOptions {

    private String model;
    private Integer maxTokens;
    private Double temperature;
    private Double topP;
    private Integer topK;
    private Double frequencyPenalty;
    private Double presencePenalty;
    private List<String> stopSequences;

    public TripleAiChatOptions() {
    }

    public TripleAiChatOptions(String model, Integer maxTokens) {
        this.model = model;
        this.maxTokens = maxTokens;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public Double getFrequencyPenalty() {
        return frequencyPenalty;
    }

    public void setFrequencyPenalty(Double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }

    @Override
    public Double getPresencePenalty() {
        return presencePenalty;
    }

    public void setPresencePenalty(Double presencePenalty) {
        this.presencePenalty = presencePenalty;
    }

    @Override
    public List<String> getStopSequences() {
        return stopSequences;
    }

    public void setStopSequences(List<String> stopSequences) {
        this.stopSequences = stopSequences;
    }

    @Override
    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    @Override
    public Double getTopP() {
        return topP;
    }

    public void setTopP(Double topP) {
        this.topP = topP;
    }

    @Override
    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    @Override
    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    @Override
    public ChatOptions.Builder<?> mutate() {
        return ChatOptions.builder()
                .model(this.model)
                .maxTokens(this.maxTokens)
                .temperature(this.temperature)
                .topP(this.topP)
                .topK(this.topK)
                .frequencyPenalty(this.frequencyPenalty)
                .presencePenalty(this.presencePenalty)
                .stopSequences(this.stopSequences);
    }

    public TripleAiChatOptions copy() {
        TripleAiChatOptions copy = new TripleAiChatOptions();
        copy.setModel(this.model);
        copy.setMaxTokens(this.maxTokens);
        copy.setTemperature(this.temperature);
        copy.setTopP(this.topP);
        copy.setTopK(this.topK);
        copy.setFrequencyPenalty(this.frequencyPenalty);
        copy.setPresencePenalty(this.presencePenalty);
        copy.setStopSequences(this.stopSequences);
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TripleAiChatOptions options = (TripleAiChatOptions) o;
        return Objects.equals(model, options.model) &&
                Objects.equals(maxTokens, options.maxTokens) &&
                Objects.equals(temperature, options.temperature) &&
                Objects.equals(topP, options.topP) &&
                Objects.equals(topK, options.topK) &&
                Objects.equals(frequencyPenalty, options.frequencyPenalty) &&
                Objects.equals(presencePenalty, options.presencePenalty) &&
                Objects.equals(stopSequences, options.stopSequences);
    }

    @Override
    public int hashCode() {
        return Objects.hash(model, maxTokens, temperature, topP, topK, frequencyPenalty, presencePenalty, stopSequences);
    }

    public static class Builder {
        private final TripleAiChatOptions options = new TripleAiChatOptions();

        public Builder model(String model) {
            options.setModel(model);
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            options.setMaxTokens(maxTokens);
            return this;
        }

        public Builder temperature(Double temperature) {
            options.setTemperature(temperature);
            return this;
        }

        public Builder topP(Double topP) {
            options.setTopP(topP);
            return this;
        }

        public Builder topK(Integer topK) {
            options.setTopK(topK);
            return this;
        }

        public Builder frequencyPenalty(Double frequencyPenalty) {
            options.setFrequencyPenalty(frequencyPenalty);
            return this;
        }

        public Builder presencePenalty(Double presencePenalty) {
            options.setPresencePenalty(presencePenalty);
            return this;
        }

        public Builder stopSequences(List<String> stopSequences) {
            options.setStopSequences(stopSequences);
            return this;
        }

        public TripleAiChatOptions build() {
            return options.copy();
        }
    }
}
