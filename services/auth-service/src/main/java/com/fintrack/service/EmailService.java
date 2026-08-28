package com.fintrack.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void sendInvitationEmail(String toEmail, String inviterName, String acceptUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setTo(toEmail);
            helper.setFrom(fromAddress);
            helper.setSubject(inviterName + " invited you to join their team on FinTrack360");
            helper.setText("""
                    <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto;">
                      <h2 style="color:#1e293b;">You're invited to FinTrack360</h2>
                      <p><strong>%s</strong> has invited you to join their company as an Accountant.</p>
                      <p>
                        <a href="%s" style="display:inline-block; padding:12px 24px; background:#3b82f6; color:white; text-decoration:none; border-radius:8px;">
                          Accept invitation
                        </a>
                      </p>
                      <p style="color:#64748b; font-size:13px;">This invitation expires in 7 days. If you weren't expecting this, you can ignore this email.</p>
                    </div>
                    """.formatted(inviterName, acceptUrl), true);

            mailSender.send(message);
            log.info("Invitation email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send invitation email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Could not send invitation email. Check SMTP configuration.", e);
        }
    }
}
