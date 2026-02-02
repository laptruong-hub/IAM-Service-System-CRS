package com.crs.iamservice.dto.request;

import lombok.Builder;

@Builder
public record UserSearchRequest(
        String keyword, // Tìm kiếm trong email, fullName, phone
        Long roleId, // Lọc theo role
        Boolean isActive, // Lọc theo trạng thái active
        Boolean isDeleted, // Lọc theo trạng thái deleted
        Integer page, // Trang hiện tại (0-indexed)
        Integer size // Số items mỗi trang
) {
    // Default values
    public UserSearchRequest {
        page = (page == null || page < 0) ? 0 : page;
        size = (size == null || size <= 0) ? 10 : size;
        size = Math.min(size, 100); // Giới hạn max 100 items
    }
}
