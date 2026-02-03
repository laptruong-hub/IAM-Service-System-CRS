package com.crs.iamservice.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdatePermissionRequest(
        @Size(min = 2, max = 100, message = "Tên permission phải từ 2 đến 100 ký tự")
        String name,

        String description,

        String action
) {}
