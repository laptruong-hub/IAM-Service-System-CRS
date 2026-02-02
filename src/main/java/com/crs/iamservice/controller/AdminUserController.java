package com.crs.iamservice.controller;

import com.crs.iamservice.dto.request.AdminPasswordResetRequest;
import com.crs.iamservice.dto.request.AdminUserRequest;
import com.crs.iamservice.dto.request.AdminUserUpdateRequest;
import com.crs.iamservice.dto.request.UserSearchRequest;
import com.crs.iamservice.dto.response.AdminUserResponse;
import com.crs.iamservice.dto.response.PageResponse;
import com.crs.iamservice.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin User Management", description = "APIs để admin quản lý tất cả user trong hệ thống")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách tất cả users", description = "Lấy danh sách users với phân trang")
    public ResponseEntity<PageResponse<AdminUserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        // Debug log để kiểm tra authorities
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("User: {}, Authorities: {}", auth.getName(), auth.getAuthorities());

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<AdminUserResponse> response = adminUserService.getAllUsers(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tìm kiếm và lọc users", description = "Tìm kiếm users theo keyword, role, trạng thái")
    public ResponseEntity<PageResponse<AdminUserResponse>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UserSearchRequest searchRequest = UserSearchRequest.builder()
                .keyword(keyword)
                .roleId(roleId)
                .isActive(isActive)
                .isDeleted(isDeleted)
                .page(page)
                .size(size)
                .build();

        PageResponse<AdminUserResponse> response = adminUserService.searchUsers(searchRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy chi tiết user", description = "Lấy thông tin chi tiết của 1 user theo ID")
    public ResponseEntity<AdminUserResponse> getUserById(@PathVariable String userId) {
        AdminUserResponse response = adminUserService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo user mới", description = "Admin tạo user mới với đầy đủ thông tin")
    public ResponseEntity<AdminUserResponse> createUser(@Valid @RequestBody AdminUserRequest request) {
        AdminUserResponse response = adminUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật user", description = "Admin cập nhật thông tin user")
    public ResponseEntity<AdminUserResponse> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        AdminUserResponse response = adminUserService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa user", description = "Xóa mềm user (set isDeleted = true)")
    public ResponseEntity<String> deleteUser(@PathVariable String userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.ok("Đã xóa user thành công");
    }

    @PatchMapping("/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Kích hoạt user", description = "Kích hoạt tài khoản user")
    public ResponseEntity<AdminUserResponse> activateUser(@PathVariable String userId) {
        AdminUserResponse response = adminUserService.activateUser(userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Vô hiệu hóa user", description = "Vô hiệu hóa tài khoản user")
    public ResponseEntity<AdminUserResponse> deactivateUser(@PathVariable String userId) {
        AdminUserResponse response = adminUserService.deactivateUser(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reset mật khẩu user", description = "Admin reset mật khẩu cho user")
    public ResponseEntity<String> resetUserPassword(
            @PathVariable String userId,
            @Valid @RequestBody AdminPasswordResetRequest request) {
        adminUserService.resetUserPassword(userId, request);
        return ResponseEntity.ok("Đã reset mật khẩu thành công");
    }
}
