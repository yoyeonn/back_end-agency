package com.travel.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class BrevoEmailService {

    private final JavaMailSender mailSender;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    public BrevoEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetPasswordEmail(String toEmail, String resetLink) {
        String subject = "Réinitialisation de votre mot de passe";

        String html = """
          <div style="font-family:Arial,sans-serif;line-height:1.6">
            <h2>Réinitialisation du mot de passe</h2>
            <p>Vous avez demandé la réinitialisation de votre mot de passe.</p>
            <p>
              <a href="%s" style="display:inline-block;padding:10px 16px;background:#1e88e5;color:#fff;text-decoration:none;border-radius:6px">
                Réinitialiser mon mot de passe
              </a>
            </p>
            <p>Ce lien expire dans <b>30 minutes</b>.</p>
            <p>Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.</p>
            <hr/>
            <small>Travel App</small>
          </div>
        """.formatted(resetLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);              // must match SMTP account in many cases
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);               // true = HTML

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send reset email", e);
        }
    }
}
