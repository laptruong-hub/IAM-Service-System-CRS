package com.crs.iamservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request xác minh mã OTP (Bước 2: nhập mã OTP đã gửi về email)
 */
public record VerifyResetCodeRequest(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email,

        @NotBlank(message = "Mã xác nhận không được để trống")
        @Size(min = 6, max = 6, message = "Mã xác nhận phải có 6 ký tự")
        String resetCode
) {
}
