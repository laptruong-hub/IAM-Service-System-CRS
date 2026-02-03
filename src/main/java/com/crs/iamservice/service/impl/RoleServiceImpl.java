package com.crs.iamservice.service.impl;

import com.crs.iamservice.dto.request.CreateRoleRequest;
import com.crs.iamservice.dto.request.UpdateRoleRequest;
import com.crs.iamservice.dto.response.PermissionResponse;
import com.crs.iamservice.dto.response.RoleResponse;
import com.crs.iamservice.entity.Permission;
import com.crs.iamservice.entity.Role;
import com.crs.iamservice.exception.ResourceAlreadyExistsException;
import com.crs.iamservice.exception.ResourceNotFoundException;
import com.crs.iamservice.repository.PermissionRepository;
import com.crs.iamservice.repository.RoleRepository;
import com.crs.iamservice.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public RoleResponse createRole(CreateRoleRequest request) {
        // Kiểm tra role đã tồn tại chưa
        if (roleRepository.findByName(request.name()).isPresent()) {
            throw new ResourceAlreadyExistsException("Role với tên '" + request.name() + "' đã tồn tại");
        }

        Set<Permission> permissions = new HashSet<>();
        if (request.permissionIds() != null && !request.permissionIds().isEmpty()) {
            permissions = new HashSet<>(permissionRepository.findAllById(request.permissionIds()));
            if (permissions.size() != request.permissionIds().size()) {
                throw new ResourceNotFoundException("Một số permission không tồn tại");
            }
        }

        Role role = Role.builder()
                .name(request.name())
                .isActive(true)
                .permissions(permissions)
                .build();

        Role savedRole = roleRepository.save(role);
        return mapToRoleResponse(savedRole);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy role với id: " + id));
        return mapToRoleResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleByName(String name) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy role với tên: " + name));
        return mapToRoleResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getActiveRoles() {
        return roleRepository.findAll().stream()
                .filter(Role::isActive)
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RoleResponse updateRole(Long id, UpdateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy role với id: " + id));

        // Cập nhật tên nếu có
        if (request.name() != null && !request.name().isBlank()) {
            // Kiểm tra tên mới có bị trùng không
            roleRepository.findByName(request.name())
                    .filter(existingRole -> !existingRole.getId().equals(id))
                    .ifPresent(existingRole -> {
                        throw new ResourceAlreadyExistsException("Role với tên '" + request.name() + "' đã tồn tại");
                    });
            role.setName(request.name());
        }

        // Cập nhật trạng thái active nếu có
        if (request.isActive() != null) {
            role.setActive(request.isActive());
        }

        // Cập nhật permissions nếu có
        if (request.permissionIds() != null) {
            Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(request.permissionIds()));
            if (permissions.size() != request.permissionIds().size()) {
                throw new ResourceNotFoundException("Một số permission không tồn tại");
            }
            role.setPermissions(permissions);
        }

        Role updatedRole = roleRepository.save(role);
        return mapToRoleResponse(updatedRole);
    }

    @Override
    public RoleResponse assignPermissionsToRole(Long roleId, List<String> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy role với id: " + roleId));

        Set<Permission> newPermissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
        if (newPermissions.size() != permissionIds.size()) {
            throw new ResourceNotFoundException("Một số permission không tồn tại");
        }

        // Thêm permissions mới vào set hiện tại
        role.getPermissions().addAll(newPermissions);
        
        Role updatedRole = roleRepository.save(role);
        return mapToRoleResponse(updatedRole);
    }

    @Override
    public RoleResponse removePermissionsFromRole(Long roleId, List<String> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy role với id: " + roleId));

        // Xóa permissions khỏi role
        role.getPermissions().removeIf(permission -> permissionIds.contains(permission.getId()));
        
        Role updatedRole = roleRepository.save(role);
        return mapToRoleResponse(updatedRole);
    }

    @Override
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy role với id: " + id));
        
        // Có thể thêm kiểm tra xem role có đang được user nào sử dụng không
        roleRepository.delete(role);
    }

    @Override
    public void activateRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy role với id: " + id));
        role.setActive(true);
        roleRepository.save(role);
    }

    @Override
    public void deactivateRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy role với id: " + id));
        role.setActive(false);
        roleRepository.save(role);
    }

    private RoleResponse mapToRoleResponse(Role role) {
        Set<PermissionResponse> permissionResponses = role.getPermissions() != null
                ? role.getPermissions().stream()
                    .map(this::mapToPermissionResponse)
                    .collect(Collectors.toSet())
                : new HashSet<>();

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .isActive(role.isActive())
                .permissions(permissionResponses)
                .build();
    }

    private PermissionResponse mapToPermissionResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .action(permission.getAction())
                .build();
    }
}
