package com.crs.iamservice.service.impl;

import com.crs.iamservice.dto.request.CreatePermissionRequest;
import com.crs.iamservice.dto.request.UpdatePermissionRequest;
import com.crs.iamservice.dto.response.PermissionResponse;
import com.crs.iamservice.entity.Permission;
import com.crs.iamservice.exception.ResourceAlreadyExistsException;
import com.crs.iamservice.exception.ResourceNotFoundException;
import com.crs.iamservice.repository.PermissionRepository;
import com.crs.iamservice.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Override
    public PermissionResponse createPermission(CreatePermissionRequest request) {
        // Kiểm tra permission đã tồn tại chưa
        if (permissionRepository.existsById(request.id())) {
            throw new ResourceAlreadyExistsException("Permission với id '" + request.id() + "' đã tồn tại");
        }

        Permission permission = Permission.builder()
                .id(request.id())
                .name(request.name())
                .description(request.description())
                .action(request.action())
                .build();

        Permission savedPermission = permissionRepository.save(permission);
        return mapToPermissionResponse(savedPermission);
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getPermissionById(String id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy permission với id: " + id));
        return mapToPermissionResponse(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(this::mapToPermissionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PermissionResponse updatePermission(String id, UpdatePermissionRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy permission với id: " + id));

        // Cập nhật các trường nếu có giá trị
        if (request.name() != null && !request.name().isBlank()) {
            permission.setName(request.name());
        }

        if (request.description() != null) {
            permission.setDescription(request.description());
        }

        if (request.action() != null && !request.action().isBlank()) {
            permission.setAction(request.action());
        }

        Permission updatedPermission = permissionRepository.save(permission);
        return mapToPermissionResponse(updatedPermission);
    }

    @Override
    public void deletePermission(String id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy permission với id: " + id));
        
        // Có thể thêm kiểm tra xem permission có đang được role nào sử dụng không
        permissionRepository.delete(permission);
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
