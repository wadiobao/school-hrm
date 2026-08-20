package com.kltn.school_hrm.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.kltn.school_hrm.dto.request.EmployeeCreateRequest;
import com.kltn.school_hrm.dto.request.WorkPermitRequest;
import com.kltn.school_hrm.dto.response.EmployeeResponse;
import com.kltn.school_hrm.dto.response.WorkPermitResponse;

public interface EmployeeService {
	EmployeeResponse createEmployee(EmployeeCreateRequest request);

	EmployeeResponse updateEmployee(Long id, EmployeeCreateRequest request);

	void deleteEmployee(Long id);

	EmployeeResponse getEmployeeById(Long id);

	Page<EmployeeResponse> searchEmployees(Long departmentId, String keyword, Pageable pageable);

	WorkPermitResponse updateWorkPermit(Long employeeId, WorkPermitRequest request);

	List<WorkPermitResponse> getExpiringWorkPermitsAndVisas(int withinDays);
}
