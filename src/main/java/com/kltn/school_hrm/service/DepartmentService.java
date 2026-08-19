package com.kltn.school_hrm.service;

import java.util.List;

import com.kltn.school_hrm.dto.request.DepartmentCreateRequest;
import com.kltn.school_hrm.dto.response.DepartmentResponse;

public interface DepartmentService {
    DepartmentResponse createDepartment(DepartmentCreateRequest request);
    DepartmentResponse updateDepartment(Long id, DepartmentCreateRequest request);
    DepartmentResponse getDepartmentById(Long id);
    List<DepartmentResponse> getAllDepartments();
    void deleteDepartment(Long id);
}
