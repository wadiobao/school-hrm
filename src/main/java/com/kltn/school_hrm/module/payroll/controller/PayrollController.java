package com.kltn.school_hrm.module.payroll.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.module.payroll.dto.request.PayrollCreateRequest;
import com.kltn.school_hrm.module.payroll.dto.response.PayrollResponse;
import com.kltn.school_hrm.module.payroll.service.PayrollService;
import com.kltn.school_hrm.shared.configuration.dto.CustomPrincipal;
import com.kltn.school_hrm.shared.enums.Enums.PayrollStatus;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payrolls")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;
    private final EmployeeRepository employeeRepository;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<PayrollResponse>> createPayroll(
            @Valid @RequestBody PayrollCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(payrollService.createPayroll(request), "Tạo bảng lương thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<PayrollResponse>> updatePayroll(
            @PathVariable Long id,
            @Valid @RequestBody PayrollCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.updatePayroll(id, request), "Cập nhật thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PayrollResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getPayrollById(id), "OK"));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER', 'BOARD_OF_DIRECTORS')")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getAllPayrolls(), "OK"));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getByEmployee(
            @PathVariable Long employeeId,
            Authentication auth) {
        if (!hasManagementAccess(auth)) {
            Long currentEmpId = getCurrentEmployeeId(auth);
            if (currentEmpId == null || !currentEmpId.equals(employeeId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Bạn chỉ được xem bảng lương của chính mình"));
            }
        }
        return ResponseEntity.ok(ApiResponse.success(payrollService.getPayrollsByEmployeeId(employeeId), "OK"));
    }

    /** Lấy danh sách bảng lương theo kỳ (Nhân viên thông thường chỉ xem được của chính mình) */
    @GetMapping("/period/{periodId}")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getByPeriodId(
            @PathVariable Long periodId,
            Authentication auth) {
        List<PayrollResponse> payrolls = payrollService.getPayrollsByPeriodId(periodId);
        if (!hasManagementAccess(auth)) {
            Long currentEmpId = getCurrentEmployeeId(auth);
            if (currentEmpId != null) {
                payrolls = payrolls.stream()
                        .filter(p -> p.getEmployeeId() != null && p.getEmployeeId().equals(currentEmpId))
                        .collect(Collectors.toList());
            } else {
                payrolls = List.of();
            }
        }
        return ResponseEntity.ok(ApiResponse.success(payrolls, "OK"));
    }

    @GetMapping("/period")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getByPeriod(
            @RequestParam Integer month,
            @RequestParam Integer year,
            Authentication auth) {
        List<PayrollResponse> payrolls = payrollService.getPayrollsByMonthAndYear(month, year);
        if (!hasManagementAccess(auth)) {
            Long currentEmpId = getCurrentEmployeeId(auth);
            if (currentEmpId != null) {
                payrolls = payrolls.stream()
                        .filter(p -> p.getEmployeeId() != null && p.getEmployeeId().equals(currentEmpId))
                        .collect(Collectors.toList());
            } else {
                payrolls = List.of();
            }
        }
        return ResponseEntity.ok(ApiResponse.success(payrolls, "OK"));
    }

    /** Endpoint tiện ích: Nhân viên tự xem danh sách bảng lương của chính mình */
    @GetMapping("/my-payrolls")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getMyPayrolls(Authentication auth) {
        Long currentEmpId = getCurrentEmployeeId(auth);
        if (currentEmpId == null) {
            return ResponseEntity.ok(ApiResponse.success(List.of(), "OK"));
        }
        return ResponseEntity.ok(ApiResponse.success(payrollService.getPayrollsByEmployeeId(currentEmpId), "OK"));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER', 'BOARD_OF_DIRECTORS')")
    public ResponseEntity<ApiResponse<PayrollResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam PayrollStatus status) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.updatePayrollStatus(id, status), "Cập nhật trạng thái thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        payrollService.deletePayroll(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa thành công"));
    }

    private boolean hasManagementAccess(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream().anyMatch(a -> {
            String role = a.getAuthority();
            return role.equals("SUPER_ADMIN") || role.equals("ROLE_SUPER_ADMIN")
                    || role.equals("HR_MANAGER") || role.equals("ROLE_HR_MANAGER")
                    || role.equals("BOARD_OF_DIRECTORS") || role.equals("ROLE_BOARD_OF_DIRECTORS");
        });
    }

    private Long getCurrentEmployeeId(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof CustomPrincipal principal) {
            return employeeRepository.findByUserId(principal.userId())
                    .map(Employee::getId)
                    .orElse(null);
        }
        return null;
    }
}
