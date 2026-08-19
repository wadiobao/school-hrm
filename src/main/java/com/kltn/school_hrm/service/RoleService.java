package com.kltn.school_hrm.service;

import java.util.List;

import com.kltn.school_hrm.dto.request.RoleCreateRequest;
import com.kltn.school_hrm.dto.response.RoleResponse;

public interface RoleService {
    RoleResponse createRole(RoleCreateRequest request);
    RoleResponse updateRole(Long id, RoleCreateRequest request);
    RoleResponse getRoleById(Long id);
    List<RoleResponse> getAllRoles();
    void deleteRole(Long id);
}
