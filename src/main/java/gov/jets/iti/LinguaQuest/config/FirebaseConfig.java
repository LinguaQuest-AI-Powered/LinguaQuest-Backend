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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.config.path:firebase-service-account.json}")
    private String firebaseConfigPath;

    @Value("${FIREBASE_CREDENTIALS:}")
    private String firebaseCredentials;

    @PostConstruct
    public void initFirebase() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try (InputStream serviceAccount = getFirebaseCredentials()) {
            if (serviceAccount == null) {
                log.warn("No Firebase credentials found. Set FIREBASE_CREDENTIALS env var or provide '{}' in classpath.", firebaseConfigPath);
                return;
            }
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setHttpTransport(new ApacheHttpTransport())
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase initialized successfully");
        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK", e);
        }
    }

    private InputStream getFirebaseCredentials() {
        if (firebaseCredentials != null && !firebaseCredentials.isBlank()) {
            log.info("Loading Firebase credentials from FIREBASE_CREDENTIALS property");
            return new ByteArrayInputStream(firebaseCredentials.getBytes(StandardCharsets.UTF_8));
        }

        try {
            log.info("Loading Firebase credentials from classpath: {}", firebaseConfigPath);
            return new ClassPathResource(firebaseConfigPath).getInputStream();
        } catch (IOException e) {
            return null;
        }
    }
}

