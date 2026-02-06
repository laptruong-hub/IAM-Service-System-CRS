package com.crs.iamservice.service;

/**
 * Service interface cho việc gửi email
 */
public interface EmailService {
    
    /**
     * Gửi email chào mừng khi user đăng ký thành công
     */
    void sendWelcomeEmail(String toEmail, String fullName);

    /**
     * Gửi email chứa mã OTP để reset mật khẩu
     * @param toEmail Email người nhận
     * @param fullName Tên đầy đủ của user
     * @param resetCode Mã OTP 6 chữ số
     */
    void sendPasswordResetEmail(String toEmail, String fullName, String resetCode);
    
    /**
     * Gửi email đơn giản (text)
     */
    void sendSimpleEmail(String toEmail, String subject, String body);
    
    /**
     * Gửi email với template HTML
     */
    void sendHtmlEmail(String toEmail, String subject, String templateName, java.util.Map<String, Object> variables);
}
