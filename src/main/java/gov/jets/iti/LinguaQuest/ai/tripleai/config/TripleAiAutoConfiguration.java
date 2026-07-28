package gov.jets.iti.LinguaQuest.ai.tripleai.config;

import gov.jets.iti.LinguaQuest.ai.tripleai.client.TripleAiApiClient;
import gov.jets.iti.LinguaQuest.ai.tripleai.model.TripleAiChatModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Spring Boot configuration for the University AI Gateway (TripleAI) integration.
 */
@Configuration
@EnableConfigurationProperties(TripleAiProperties.class)
public class TripleAiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TripleAiApiClient tripleAiApiClient(TripleAiProperties properties) {
        return new TripleAiApiClient(properties);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(TripleAiChatModel.class)
    public ChatModel tripleAiChatModel(TripleAiApiClient apiClient, TripleAiProperties properties) {
        return new TripleAiChatModel(apiClient, properties);
    }
}
