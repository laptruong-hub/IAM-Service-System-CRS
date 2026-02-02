package com.crs.iamservice.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Builder
public record AdminUserResponse(
        String userId,
        String email,
        String fullName,
        String phone,
        String gender,
        LocalDate dob,
        boolean isActive,
        boolean isDeleted,
        LocalDateTime createdAt,
        RoleInfo role) {
    @Builder
    public record RoleInfo(
            Long id,
            String name,
            Set<String> permissions) {
    }
}
