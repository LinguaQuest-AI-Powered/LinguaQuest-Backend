package gov.jets.iti.LinguaQuest.service;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Async("mailExecutor")
    public void sendOtpEmail(String email, String otp) {
        try {

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Verify your LinguaQuest account");

            String html = """
                                        <!DOCTYPE html>
                                        <html lang="en">
                                        <head>
                                        <meta charset="UTF-8">
                                        <title>Verify your email</title>
                                        </head>
                    
                                        <body style="margin:0;padding:40px 0;background:#EEF7FB;font-family:Arial,Helvetica,sans-serif;">
                    
                                        <table role="presentation" width="100%%" cellspacing="0" cellpadding="0">
                                        <tr>
                                        <td align="center">
                    
                                        <table role="presentation"
                                               width="600"
                                               cellspacing="0"
                                               cellpadding="0"
                                               style="background:#FFFFFF;border-radius:28px;overflow:hidden;
                                                      box-shadow:0 8px 24px rgba(0,0,0,.08);">
                    
                                        <tr>
                                        <td align="center" style="padding:40px 40px 20px;">
                    
                                        <h1 style="margin:20px 0 10px;
                                                   color:#9C5A00;
                                                   font-size:34px;
                                                   font-weight:bold;">
                    
                                        Verify your email
                    
                                        </h1>
                    
                                        <p style="margin:0;
                                                  color:#666666;
                                                  font-size:17px;
                                                  line-height:28px;">
                    
                                        Welcome to <strong>LinguaQuest</strong>!<br>
                                        We received a request to verify your account.<br>
                                        Use the verification code below to continue your language learning adventure.
                    
                                        </p>
                    
                                        </td>
                                        </tr>
                    
                                        <tr>
                                        <td align="center" style="padding:25px 0;">
                    
                                        <table role="presentation" cellspacing="10" cellpadding="0">
                    <tr>
                    %s
                    </tr>
                                        </table>
                    
                                        </td>
                                        </tr>
                    
                                        <tr>
                                        <td style="padding:10px 50px 40px;">
                    
                                        <p style="margin:0;
                                                  color:#555555;
                                                  font-size:15px;
                                                  line-height:28px;
                                                  text-align:center;">
                    
                                        ⏰ This verification code is valid for <strong>5 minutes</strong>.
                    
                                        <br><br>
                    
                                        If you didn't request this email,
                                        you can safely ignore this it.
                    
                                        </p>
                    
                                        </td>
                                        </tr>
                    
                                        <tr>
                                        <td style="
                                        background:#F8F8F8;
                                        padding:25px;
                                        text-align:center;
                                        font-size:13px;
                                        color:#888888;
                                        ">
                    
                                        <strong>LinguaQuest</strong><br>
                                        Learn languages. Explore the world.<br><br>
                    
                                        © 2026 LinguaQuest. All rights reserved.
                    
                                        </td>
                                        </tr>
                    
                                        </table>
                    
                                        </td>
                                        </tr>
                                        </table>
                    
                                        </body>
                                        </html>
                    """;

            helper.setText(html, true);
            helper.setText(html.formatted(buildOtpBoxes(otp)), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to {}", email);
        }

    }

    private String buildOtpBoxes(String otp) {
        StringBuilder boxes = new StringBuilder();

        for (char c : otp.toCharArray()) {
            boxes.append("""
                    <td style="
                        width:56px;
                        height:56px;
                        border:2px solid #D9E7F2;
                        border-radius:14px;
                        font-size:28px;
                        font-weight:bold;
                        text-align:center;
                        color:#F59E0B;
                        font-family:Arial;
                    ">
                    """).append(c).append("</td>");
        }

        return boxes.toString();
    }
}