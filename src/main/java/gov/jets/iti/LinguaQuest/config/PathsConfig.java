package gov.jets.iti.LinguaQuest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PathsConfig {

    @Bean
    List<String> publicPaths() {
        return List.of("/api/v1/auth/login");
    }

    @Bean
    List<String> privatePaths() {
        return List.of("");
    }
}
