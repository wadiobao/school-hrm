package com.kltn.school_hrm.module.core.service;

import java.util.List;

import com.kltn.school_hrm.module.core.dto.request.RoleCreateRequest;
import com.kltn.school_hrm.module.core.dto.response.RoleResponse;

public interface RoleService {
    RoleResponse createRole(RoleCreateRequest request);
    RoleResponse updateRole(Long id, RoleCreateRequest request);
    RoleResponse getRoleById(Long id);
    List<RoleResponse> getAllRoles();
    void deleteRole(Long id);
}
