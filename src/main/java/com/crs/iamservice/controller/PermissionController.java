package com.crs.iamservice.controller;

import com.crs.iamservice.dto.request.CreatePermissionRequest;
import com.crs.iamservice.dto.request.UpdatePermissionRequest;
import com.crs.iamservice.dto.response.PermissionResponse;
import com.crs.iamservice.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * Tạo permission mới
     */
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_PERMISSIONS')")
    public ResponseEntity<PermissionResponse> createPermission(
            @Valid @RequestBody CreatePermissionRequest request) {
        PermissionResponse response = permissionService.createPermission(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lấy permission theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_PERMISSIONS')")
    public ResponseEntity<PermissionResponse> getPermissionById(@PathVariable String id) {
        PermissionResponse response = permissionService.getPermissionById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy tất cả permissions
     */
    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_PERMISSIONS')")
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        List<PermissionResponse> response = permissionService.getAllPermissions();
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật permission
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_PERMISSIONS')")
    public ResponseEntity<PermissionResponse> updatePermission(
            @PathVariable String id,
            @Valid @RequestBody UpdatePermissionRequest request) {
        PermissionResponse response = permissionService.updatePermission(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa permission
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_PERMISSIONS')")
    public ResponseEntity<Void> deletePermission(@PathVariable String id) {
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }
}
