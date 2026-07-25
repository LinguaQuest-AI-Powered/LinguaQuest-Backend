package gov.jets.iti.LinguaQuest.service.mail;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(name = "mail.provider", havingValue = "gmail-api", matchIfMissing = true)
public class GmailApiMailService extends AbstractMailService {

    @Value("${gmail.client-id:}")
    private String clientId;

    @Value("${gmail.client-secret:}")
    private String clientSecret;

    @Value("${gmail.refresh-token:}")
    private String refreshToken;

    @Value("${gmail.sender-email:}")
    private String senderEmail;

    private final RestClient restClient = RestClient.create();

    @Async("mailExecutor")
    @Override
    public void sendOtpEmail(String email, String otp) {
        try {
            // 1. Fetch short-lived OAuth access token
            String accessToken = getAccessToken();

            // 2. Build MIME Message with shared HTML layout & logo
            MimeMessage message = buildOtpMimeMessage(null, email, otp);
            if (StringUtils.hasText(senderEmail)) {
                message.setFrom(new InternetAddress(senderEmail, "LinguaQuest"));
            }

            // 3. Convert MimeMessage to URL-safe Base64
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            message.writeTo(buffer);
            String rawMessage = Base64.getUrlEncoder().encodeToString(buffer.toByteArray());

            // 4. Send via Gmail REST API over HTTPS (Port 443)
            restClient.post()
                    .uri("https://gmail.googleapis.com/gmail/v1/users/me/messages/send")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("raw", rawMessage))
                    .retrieve()
                    .toBodilessEntity();

            log.info("OTP email sent successfully via Gmail API to {}", email);

        } catch (Exception e) {
            log.error("Failed to send OTP email via Gmail API to {}", email, e);
        }
    }

    @SuppressWarnings("unchecked")
    private String getAccessToken() {
        Map<String, Object> response = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("client_id=" + clientId +
                      "&client_secret=" + clientSecret +
                      "&refresh_token=" + refreshToken +
                      "&grant_type=refresh_token")
                .retrieve()
                .body(Map.class);

        if (response != null && response.containsKey("access_token")) {
            return (String) response.get("access_token");
        }
        throw new IllegalStateException("Failed to obtain OAuth2 access token from Google");
    }
}
