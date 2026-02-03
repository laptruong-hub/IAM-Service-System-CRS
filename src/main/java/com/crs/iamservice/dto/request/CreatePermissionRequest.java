package com.crs.iamservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreatePermissionRequest(
        @NotBlank(message = "ID permission không được để trống")
        @Size(min = 2, max = 50, message = "ID permission phải từ 2 đến 50 ký tự")
        @Pattern(regexp = "^[A-Z_]+$", message = "ID permission phải viết hoa và dùng dấu gạch dưới (VD: CREATE_USER)")
        String id,

        @NotBlank(message = "Tên permission không được để trống")
        @Size(min = 2, max = 100, message = "Tên permission phải từ 2 đến 100 ký tự")
        String name,

        String description,

        @NotBlank(message = "Action không được để trống")
        String action
) {}
