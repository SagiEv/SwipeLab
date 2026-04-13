package com.swipelab.auth.application;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.spring6.SpringTemplateEngine;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendVerificationEmail_ShouldNotThrowException() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@swipelab.com");
        ReflectionTestUtils.setField(emailService, "baseUrl", "http://localhost:8080");

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any())).thenReturn("<html>Verify</html>");

        assertDoesNotThrow(() -> emailService.sendVerificationEmail("test@swipelab.com", "token-123"));

        verify(templateEngine, times(1)).process(eq("email/verification-email"), any());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail_ShouldNotThrowException() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@swipelab.com");
        ReflectionTestUtils.setField(emailService, "baseUrl", "http://localhost:8080");

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any())).thenReturn("<html>Reset</html>");

        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail("test@swipelab.com", "token-123"));

        verify(templateEngine, times(1)).process(eq("email/password-reset-email"), any());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendInvitationEmail_ShouldNotThrowException() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@swipelab.com");
        ReflectionTestUtils.setField(emailService, "baseUrl", "http://localhost:8080");

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any())).thenReturn("<html>Invite</html>");

        assertDoesNotThrow(() -> emailService.sendInvitationEmail("admin@swipelab.com", "ADMIN", "invite-token-456"));

        verify(templateEngine, times(1)).process(eq("email/invitation-email"), any());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}
