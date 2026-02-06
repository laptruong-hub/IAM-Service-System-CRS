package com.crs.iamservice.controller;

import com.crs.iamservice.dto.request.CreateRoleRequest;
import com.crs.iamservice.dto.request.UpdateRoleRequest;
import com.crs.iamservice.dto.response.RoleResponse;
import com.crs.iamservice.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * Tạo role mới
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        RoleResponse response = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lấy role theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable Long id) {
        RoleResponse response = roleService.getRoleById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy role theo tên
     */
    @GetMapping("/name/{name}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<RoleResponse> getRoleByName(@PathVariable String name) {
        RoleResponse response = roleService.getRoleByName(name);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy tất cả roles
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> response = roleService.getAllRoles();
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy tất cả roles đang active
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<List<RoleResponse>> getActiveRoles() {
        List<RoleResponse> response = roleService.getActiveRoles();
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật role
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        RoleResponse response = roleService.updateRole(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Gán permissions cho role
     */
    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<RoleResponse> assignPermissions(
            @PathVariable Long id,
            @RequestBody List<String> permissionIds) {
        RoleResponse response = roleService.assignPermissionsToRole(id, permissionIds);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa permissions khỏi role
     */
    @DeleteMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<RoleResponse> removePermissions(
            @PathVariable Long id,
            @RequestBody List<String> permissionIds) {
        RoleResponse response = roleService.removePermissionsFromRole(id, permissionIds);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa role
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Kích hoạt role
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<String> activateRole(@PathVariable Long id) {
        roleService.activateRole(id);
        return ResponseEntity.ok("Role đã được kích hoạt");
    }

    /**
     * Vô hiệu hóa role
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<String> deactivateRole(@PathVariable Long id) {
        roleService.deactivateRole(id);
        return ResponseEntity.ok("Role đã bị vô hiệu hóa");
    }
}
