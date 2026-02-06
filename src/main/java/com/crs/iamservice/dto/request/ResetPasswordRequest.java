package com.crs.iamservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request đặt mật khẩu mới (Bước 3: sau khi xác minh OTP thành công)
 */
public record ResetPasswordRequest(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email,

        @NotBlank(message = "Mã xác nhận không được để trống")
        @Size(min = 6, max = 6, message = "Mã xác nhận phải có 6 ký tự")
        String resetCode,

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
        String newPassword
) {
}
