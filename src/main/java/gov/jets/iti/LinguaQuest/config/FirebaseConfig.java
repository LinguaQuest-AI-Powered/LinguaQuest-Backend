package gov.jets.iti.LinguaQuest.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.config.path:firebase-service-account.json}")
    private String firebaseConfigPath;

    @PostConstruct
    public void initFirebase() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try (InputStream serviceAccount = new ClassPathResource(firebaseConfigPath).getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setHttpTransport(new ApacheHttpTransport())
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase initialized successfully");
        } catch (java.io.FileNotFoundException e) {
            log.warn("Firebase service account credentials file '{}' not found in classpath. Firebase endpoints will throw errors when called.", firebaseConfigPath);
        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK", e);
        }
    }
}
