package com.crs.iamservice.service.impl;

import com.crs.iamservice.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${spring.application.name:Car Rental System}")
    private String applicationName;
    
    @Override
    public void sendWelcomeEmail(String toEmail, String fullName) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("fullName", fullName);
        variables.put("email", toEmail);
        variables.put("applicationName", "Car Rental System");
        variables.put("supportEmail", fromEmail);
        variables.put("year", java.time.Year.now().getValue());
        
        String subject = "🎉 Chào mừng bạn đến với Car Rental System!";
        
        sendHtmlEmail(toEmail, subject, "email/welcome-email", variables);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String fullName, String resetCode) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("fullName", fullName);
        variables.put("email", toEmail);
        variables.put("resetCode", resetCode);
        variables.put("expiryMinutes", 5);
        variables.put("applicationName", "Car Rental System");
        variables.put("supportEmail", fromEmail);
        variables.put("year", java.time.Year.now().getValue());

        String subject = "🔐 Mã xác nhận đặt lại mật khẩu - Car Rental System";

        sendHtmlEmail(toEmail, subject, "email/password-reset", variables);
    }
    
    @Override
    public void sendSimpleEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send simple email to: {}. Error: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    @Override
    public void sendHtmlEmail(String toEmail, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            // Set thông tin email
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            
            // Xử lý template Thymeleaf
            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process(templateName, context);
            
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            log.info("HTML email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}. Error: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
