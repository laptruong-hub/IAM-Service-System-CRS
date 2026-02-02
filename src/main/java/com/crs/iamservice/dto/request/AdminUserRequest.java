package com.crs.iamservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record AdminUserRequest(
        @NotBlank(message = "Email không được để trống") @Email(message = "Email không hợp lệ") String email,

        @NotBlank(message = "Họ tên không được để trống") @Size(min = 2, max = 100, message = "Họ tên phải từ 2-100 ký tự") String fullName,

        @NotBlank(message = "Mật khẩu không được để trống") @Size(min = 6, message = "Mật khẩu phải ít nhất 6 ký tự") String password,

        String phone,

        String gender,

        LocalDate dob,

        @NotNull(message = "Role ID không được để trống") Long roleId,

        Boolean isActive // Default sẽ là true nếu null
) {
}
