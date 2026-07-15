package com.crs.iamservice.dto.response;

import lombok.Builder;

@Builder
public record UserResponse(
        String id,
        String email,
        String fullName,
        String phone,
        String dob,
        String gender,
        String createdAt,
        String role,
        boolean isActive
) {}