package com.crs.iamservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Set;

@Builder
public record CreateRoleRequest(
        @NotBlank(message = "Tên role không được để trống")
        @Size(min = 2, max = 50, message = "Tên role phải từ 2 đến 50 ký tự")
        String name,

        Set<String> permissionIds
) {}
