package com.crs.iamservice.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Set;

@Builder
public record UpdateRoleRequest(
        @Size(min = 2, max = 50, message = "Tên role phải từ 2 đến 50 ký tự")
        String name,

        Boolean isActive,

        Set<String> permissionIds
) {}
