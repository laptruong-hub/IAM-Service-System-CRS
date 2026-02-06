package com.crs.iamservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request gửi yêu cầu đặt lại mật khẩu (Bước 1: gửi mã OTP về email)
 */
public record ForgotPasswordRequest(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email
) {
}
