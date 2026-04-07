package com.swipelab.auth.application;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Async
    public void sendVerificationEmail(String to, String token) {
        String verificationLink = baseUrl + "/api/v1/auth/verify-email?token=" + token;
        
        Context context = new Context();
        context.setVariable("verificationLink", verificationLink);
        
        String htmlBody = templateEngine.process("email/verification-email", context);
        sendHtmlEmail(to, "Verify your SwipeLab Account", htmlBody);
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {
        String resetLink = baseUrl + "/auth/password/reset?token=" + token;
        
        Context context = new Context();
        context.setVariable("resetLink", resetLink);
        
        String htmlBody = templateEngine.process("email/password-reset-email", context);
        sendHtmlEmail(to, "Password Reset Request for SwipeLab", htmlBody);
    }

    @Async
    public void sendInvitationEmail(String to, String role, String token) {
        // Assume front-end or back-end has a specialized endpoint for handling invites
        String invitationLink = baseUrl + "/auth/invitation/accept?token=" + token;
        
        Context context = new Context();
        context.setVariable("invitationLink", invitationLink);
        context.setVariable("role", role != null ? role : "Platform Member");
        
        String htmlBody = templateEngine.process("email/invitation-email", context);
        sendHtmlEmail(to, "You've been invited to SwipeLab!", htmlBody);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indicates HTML format

            try {
                org.springframework.core.io.ClassPathResource logoResource = new org.springframework.core.io.ClassPathResource("static/images/logo.png");
                if (logoResource.exists()) {
                    helper.addInline("logoImage", logoResource);
                }
            } catch (Exception e) {
                log.warn("Could not attach logo image to email", e);
            }

            mailSender.send(message);
            log.info("Successfully sent email [{}] to {}", subject, to);
            
        } catch (MessagingException e) {
            log.error("Failed to send email [{}] to {}", subject, to, e);
        }
    }
}