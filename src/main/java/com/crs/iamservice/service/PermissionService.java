package com.crs.iamservice.service;

import com.crs.iamservice.dto.request.CreatePermissionRequest;
import com.crs.iamservice.dto.request.UpdatePermissionRequest;
import com.crs.iamservice.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {
    
    PermissionResponse createPermission(CreatePermissionRequest request);
    
    PermissionResponse getPermissionById(String id);
    
    List<PermissionResponse> getAllPermissions();
    
    PermissionResponse updatePermission(String id, UpdatePermissionRequest request);
    
    void deletePermission(String id);
}
