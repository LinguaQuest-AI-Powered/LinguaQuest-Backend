package gov.jets.iti.LinguaQuest.service.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mail.provider", havingValue = "smtp")
public class SmtpMailService extends AbstractMailService {

    private final JavaMailSender mailSender;

    @Async("mailExecutor")
    @Override
    public void sendOtpEmail(String email, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            buildOtpMimeMessage(message, email, otp);
            mailSender.send(message);
            log.info("OTP email sent successfully via SMTP to {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email via SMTP to {}", email, e);
        }
    }
}
