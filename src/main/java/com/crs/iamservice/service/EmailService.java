package com.crs.iamservice.service;

/**
 * Service interface cho việc gửi email
 */
public interface EmailService {
    
    /**
     * Gửi email chào mừng khi user đăng ký thành công
     * @param toEmail Email người nhận
     * @param fullName Tên đầy đủ của user
     */
    void sendWelcomeEmail(String toEmail, String fullName);
    
    /**
     * Gửi email đơn giản (text)
     * @param toEmail Email người nhận
     * @param subject Tiêu đề email
     * @param body Nội dung email
     */
    void sendSimpleEmail(String toEmail, String subject, String body);
    
    /**
     * Gửi email với template HTML
     * @param toEmail Email người nhận
     * @param subject Tiêu đề email
     * @param templateName Tên template
     * @param variables Các biến truyền vào template
     */
    void sendHtmlEmail(String toEmail, String subject, String templateName, java.util.Map<String, Object> variables);
}
