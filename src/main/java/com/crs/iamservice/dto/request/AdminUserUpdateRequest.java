package com.crs.iamservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record AdminUserUpdateRequest(
        @Email(message = "Email không hợp lệ") String email,

        @Size(min = 2, max = 100, message = "Họ tên phải từ 2-100 ký tự") String fullName,

        String phone,

        String gender,

        LocalDate dob,

        Long roleId,

        Boolean isActive

// Password không có ở đây - dùng endpoint riêng để reset password
) {
}
