package com.kltn.school_hrm.module.core.service;

import java.util.List;

import com.kltn.school_hrm.module.core.dto.request.DepartmentCreateRequest;
import com.kltn.school_hrm.module.core.dto.response.DepartmentResponse;

public interface DepartmentService {
    DepartmentResponse createDepartment(DepartmentCreateRequest request);
    DepartmentResponse updateDepartment(Long id, DepartmentCreateRequest request);
    DepartmentResponse getDepartmentById(Long id);
    List<DepartmentResponse> getAllDepartments();
    void deleteDepartment(Long id);
    DepartmentResponse updateDepartmentManager(Long departmentId, Long employeeId);
}
