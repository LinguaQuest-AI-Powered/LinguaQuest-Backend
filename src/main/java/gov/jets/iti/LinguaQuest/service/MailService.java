package gov.jets.iti.LinguaQuest.service;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Async("mailExecutor")
    public void sendOtpEmail(String email, String otp) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

        helper.setTo(email);
        helper.setSubject("Verify your LinguaQuest account");

        String html = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; background:#f5f5f5; padding:40px;">
                    <div style="
                        max-width:600px;
                        margin:auto;
                        background:white;
                        border-radius:10px;
                        padding:40px;
                        box-shadow:0 2px 8px rgba(0,0,0,.1);
                    ">

                        <h2 style="color:#2E86DE;">
                            Welcome to LinguaQuest!
                        </h2>

                        <p>Use the following verification code to continue:</p>

                        <div style="
                            font-size:32px;
                            font-weight:bold;
                            letter-spacing:8px;
                            text-align:center;
                            background:#F3F6FA;
                            padding:20px;
                            border-radius:8px;
                            margin:30px 0;
                        ">
                            %s
                        </div>

                        <p>This code will expire in <strong>5 minutes</strong>.</p>

                        <p>If you didn't request this email, you can safely ignore it.</p>

                        <hr>

                        <p style="color:gray;font-size:12px;">
                            © 2026 LinguaQuest
                        </p>

                    </div>
                </body>
                </html>
                """.formatted(otp);

        helper.setText(html, true);

        mailSender.send(message);
    }
}