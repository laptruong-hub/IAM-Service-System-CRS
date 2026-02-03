package com.crs.iamservice.service;

import com.crs.iamservice.dto.request.CreateRoleRequest;
import com.crs.iamservice.dto.request.UpdateRoleRequest;
import com.crs.iamservice.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {
    
    RoleResponse createRole(CreateRoleRequest request);
    
    RoleResponse getRoleById(Long id);
    
    RoleResponse getRoleByName(String name);
    
    List<RoleResponse> getAllRoles();
    
    List<RoleResponse> getActiveRoles();
    
    RoleResponse updateRole(Long id, UpdateRoleRequest request);
    
    RoleResponse assignPermissionsToRole(Long roleId, List<String> permissionIds);
    
    RoleResponse removePermissionsFromRole(Long roleId, List<String> permissionIds);
    
    void deleteRole(Long id);
    
    void activateRole(Long id);
    
    void deactivateRole(Long id);
}
