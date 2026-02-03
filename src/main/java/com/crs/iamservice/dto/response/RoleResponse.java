package com.crs.iamservice.dto.response;

import lombok.Builder;

import java.util.Set;

@Builder
public record RoleResponse(
        Long id,
        String name,
        boolean isActive,
        Set<PermissionResponse> permissions
) {}
