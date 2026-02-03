package com.crs.iamservice.service;

import com.crs.iamservice.dto.request.AdminPasswordResetRequest;
import com.crs.iamservice.dto.request.AdminUserRequest;
import com.crs.iamservice.dto.request.AdminUserUpdateRequest;
import com.crs.iamservice.dto.request.UserSearchRequest;
import com.crs.iamservice.dto.response.AdminUserResponse;
import com.crs.iamservice.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    /**
     * Lấy tất cả users với phân trang
     */
    PageResponse<AdminUserResponse> getAllUsers(Pageable pageable);

    /**
     * Tìm kiếm và lọc users
     */
    PageResponse<AdminUserResponse> searchUsers(UserSearchRequest request);

    /**
     * Lấy thông tin chi tiết 1 user
     */
    AdminUserResponse getUserById(String userId);

    /**
     * Tạo user mới (bởi admin)
     */
    AdminUserResponse createUser(AdminUserRequest request);

    /**
     * Cập nhật thông tin user
     */
    AdminUserResponse updateUser(String userId, AdminUserUpdateRequest request);

    /**
     * Xóa mềm user (set isDeleted = true)
     */
    void deleteUser(String userId);

    /**
     * Kích hoạt tài khoản user
     */
    AdminUserResponse activateUser(String userId);

    /**
     * Vô hiệu hóa tài khoản user
     */
    AdminUserResponse deactivateUser(String userId);

    /**
     * Admin reset mật khẩu cho user
     */
    void resetUserPassword(String userId, AdminPasswordResetRequest request);
}
