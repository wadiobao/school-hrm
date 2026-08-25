package com.kltn.school_hrm.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kltn.school_hrm.dto.common.ApiResponse;
import com.kltn.school_hrm.dto.request.EmployeeCreateRequest;
import com.kltn.school_hrm.dto.request.WorkPermitRequest;
import com.kltn.school_hrm.dto.response.EmployeeResponse;
import com.kltn.school_hrm.dto.response.WorkPermitResponse;
import com.kltn.school_hrm.service.EmployeeLifecycleService;
import com.kltn.school_hrm.service.EmployeeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

	private final EmployeeService employeeService;
	private final EmployeeLifecycleService employeeLifecycleService;

	@PostMapping
	public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
			@Valid @RequestBody EmployeeCreateRequest request) {
		EmployeeResponse response = employeeService.createEmployee(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(response, "Thêm mới nhân viên thành công"));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(@PathVariable Long id) {
		EmployeeResponse response = employeeService.getEmployeeById(id);
		return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin nhân viên thành công"));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
			@PathVariable Long id,
			@Valid @RequestBody EmployeeCreateRequest request) {
		EmployeeResponse response = employeeService.updateEmployee(id, request);
		return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật nhân viên thành công"));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
		employeeService.deleteEmployee(id);
		return ResponseEntity.ok(ApiResponse.success(null, "Xóa nhân viên thành công"));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<Page<EmployeeResponse>>> searchEmployees(
			@RequestParam(required = false) Long departmentId,
			@RequestParam(required = false) String keyword,
			@PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

		Page<EmployeeResponse> response = employeeService.searchEmployees(departmentId, keyword, pageable);
		return ResponseEntity.ok(ApiResponse.success(response, "Truy vấn danh sách nhân viên thành công"));
	}

	@PutMapping("/{id}/work-permit")
	public ResponseEntity<ApiResponse<WorkPermitResponse>> updateWorkPermit(
			@PathVariable Long id,
			@RequestBody WorkPermitRequest request) {

		WorkPermitResponse response = employeeService.updateWorkPermit(id, request);
		return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật Work Permit/Visa thành công"));
	}

	@GetMapping("/work-permits/expiring")
	public ResponseEntity<ApiResponse<List<WorkPermitResponse>>> getExpiringWorkPermits(
			@RequestParam(defaultValue = "30") int withinDays) {

		List<WorkPermitResponse> response = employeeService.getExpiringWorkPermitsAndVisas(withinDays);
		return ResponseEntity
				.ok(ApiResponse.success(response, "Lấy danh sách Visa/Work Permit sắp hết hạn thành công"));
	}

	@PostMapping("/{id}/complete-probation")
	public ResponseEntity<ApiResponse<Void>> completeProbation(@PathVariable Long id, @RequestBody String reason) {
		employeeLifecycleService.completeProbation(id, reason);
		return ResponseEntity.ok(ApiResponse.success(null, "Cập nhật trạng thái nhân viên hoàn thành thử việc"));
	}

	@PostMapping("/{id}/resume")
	public ResponseEntity<ApiResponse<Void>> resume(@PathVariable Long id, @RequestBody String reason) {
		employeeLifecycleService.resume(id, reason);
		return ResponseEntity.ok(ApiResponse.success(null, "Nhân viên đã quay lại làm việc"));
	}

	@PostMapping("/{id}/suspend")
	public ResponseEntity<ApiResponse<Void>> suspend(@PathVariable Long id, @RequestBody String reason) {
		employeeLifecycleService.suspend(id, reason);
		return ResponseEntity.ok(ApiResponse.success(null, "Nhân viên đã tạm dừng làm việc"));
	}

	@PostMapping("/{id}/resign")
	public ResponseEntity<ApiResponse<Void>> resign(@PathVariable Long id, @RequestBody String reason) {
		employeeLifecycleService.resign(id, reason);
		return ResponseEntity.ok(ApiResponse.success(null, "Nhân viên đã nghỉ việc"));
	}

	@PostMapping("/{id}/retire")
	public ResponseEntity<ApiResponse<Void>> retire(@PathVariable Long id, @RequestBody String reason) {
		employeeLifecycleService.retire(id, reason);
		return ResponseEntity.ok(ApiResponse.success(null, "Nhân viên đã nghỉ hưu"));
	}
}
