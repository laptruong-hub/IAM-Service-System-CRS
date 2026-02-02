package com.crs.iamservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AdminPasswordResetRequest(
        @NotBlank(message = "Mật khẩu mới không được để trống") @Size(min = 6, message = "Mật khẩu phải ít nhất 6 ký tự") String newPassword) {
}
