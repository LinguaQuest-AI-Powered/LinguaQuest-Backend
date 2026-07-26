package gov.jets.iti.LinguaQuest.service;

public interface MailService {

    void sendOtpEmail(String email, String otp);
}