package com.crs.iamservice.controller;

import com.crs.iamservice.dto.response.AdminUserResponse;
import com.crs.iamservice.entity.User;
import com.crs.iamservice.exception.ResourceNotFoundException;
import com.crs.iamservice.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * InternalUserController — API nội bộ dành cho service-to-service
 * communication.
 * ⚠️ KHÔNG yêu cầu JWT — được whitelist trong SecurityConfig.
 * ⚠️ KHÔNG expose qua API Gateway — chỉ gọi trực tiếp từ microservices khác.
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Internal - User API", description = "API nội bộ cho service-to-service, không cần auth")
public class InternalUserController {

    private final UserRepository userRepository;

    /**
     * Lấy thông tin user theo userId.
     * Sử dụng bởi booking-service để validate user tồn tại.
     */
    @GetMapping("/{userId}")
    @Operation(summary = "[INTERNAL] Lấy thông tin user theo ID")
    public ResponseEntity<AdminUserResponse> getUserById(@PathVariable String userId) {
        log.debug("[INTERNAL] Đang lấy thông tin user: {}", userId);
        User user = userRepository.findByIdWithRole(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với ID: " + userId));
        return ResponseEntity.ok(toResponse(user));
    }

    /**
     * Lấy danh sách users theo role name (ví dụ: DRIVER, STAFF, CUSTOMER).
     * Sử dụng bởi booking-service để lấy danh sách tài xế từ IAM.
     */
    @GetMapping("/role/{roleName}")
    @Operation(summary = "[INTERNAL] Lấy users theo role name")
    public ResponseEntity<List<AdminUserResponse>> getUsersByRole(@PathVariable String roleName) {
        log.debug("[INTERNAL] Đang lấy users với role: {}", roleName);
        List<User> users = userRepository.findActiveUsersByRoleName(roleName);
        List<AdminUserResponse> responses = users.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Kiểm tra userId có tồn tại không (trả 200 nếu có, 404 nếu không).
     * Sử dụng bởi booking-service để validate userId khi tạo booking.
     */
    @GetMapping("/{userId}/exists")
    @Operation(summary = "[INTERNAL] Kiểm tra user có tồn tại không")
    public ResponseEntity<Boolean> existsById(@PathVariable String userId) {
        boolean exists = userRepository.existsById(userId);
        return ResponseEntity.ok(exists);
    }

    // ====================================================
    // PRIVATE HELPER
    // ====================================================

    private AdminUserResponse toResponse(User user) {
        AdminUserResponse.RoleInfo roleInfo = null;
        if (user.getRole() != null) {
            Set<String> permissions = user.getRole().getPermissions() != null
                    ? user.getRole().getPermissions().stream()
                            .map(p -> p.getId())
                            .collect(Collectors.toSet())
                    : Set.of();
            roleInfo = AdminUserResponse.RoleInfo.builder()
                    .id(user.getRole().getId())
                    .name(user.getRole().getName())
                    .permissions(permissions)
                    .build();
        }
        return AdminUserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .gender(user.getGender())
                .dob(user.getDob())
                .isActive(user.isActive())
                .isDeleted(user.isDeleted())
                .createdAt(user.getCreatedAt())
                .role(roleInfo)
                .build();
    }
}
