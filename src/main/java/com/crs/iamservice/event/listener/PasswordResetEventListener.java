package com.crs.iamservice.event.listener;

import com.crs.iamservice.event.PasswordResetEvent;
import com.crs.iamservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener xử lý sự kiện reset mật khẩu
 * Sử dụng @Async để không block luồng chính (user không cần chờ gửi mail xong)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PasswordResetEventListener {

    private final EmailService emailService;

    @Async
    @EventListener
    public void handlePasswordResetEvent(PasswordResetEvent event) {
        log.info("Received password reset event for email: {}", event.getEmail());

        try {
            emailService.sendPasswordResetEmail(event.getEmail(), event.getFullName(), event.getResetCode());
            log.info("Password reset email sent successfully to: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}. Error: {}", event.getEmail(), e.getMessage());
        }
    }
}
