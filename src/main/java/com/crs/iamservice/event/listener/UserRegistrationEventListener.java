package com.crs.iamservice.event.listener;

import com.crs.iamservice.event.UserRegistrationEvent;
import com.crs.iamservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener xử lý sự kiện đăng ký tài khoản
 * Sử dụng @Async để không block luồng chính
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationEventListener {
    
    private final EmailService emailService;
    
    @Async
    @EventListener
    public void handleUserRegistrationEvent(UserRegistrationEvent event) {
        log.info("Received user registration event for email: {}", event.getEmail());
        
        try {
            emailService.sendWelcomeEmail(event.getEmail(), event.getFullName());
            log.info("Welcome email sent successfully to: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}. Error: {}", event.getEmail(), e.getMessage());
            // Không throw exception để không ảnh hưởng đến các listener khác
        }
    }
}
