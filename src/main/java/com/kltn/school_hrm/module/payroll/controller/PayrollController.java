package com.kltn.school_hrm.module.payroll.controller;

import java.util.List;

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
import com.kltn.school_hrm.module.payroll.dto.request.PayrollCreateRequest;
import com.kltn.school_hrm.module.payroll.dto.response.PayrollResponse;
import com.kltn.school_hrm.module.payroll.service.PayrollService;
import com.kltn.school_hrm.shared.enums.Enums.PayrollStatus;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payrolls")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping
    public ResponseEntity<ApiResponse<PayrollResponse>> createPayroll(
            @Valid @RequestBody PayrollCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(payrollService.createPayroll(request), "Tạo bảng lương thành công"));
    }

    @PutMapping("/{id}")
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
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getAllPayrolls(), "OK"));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getPayrollsByEmployeeId(employeeId), "OK"));
    }

    @GetMapping("/period/{periodId}")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getByPeriodId(@PathVariable Long periodId) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getPayrollsByPeriodId(periodId), "OK"));
    }

    @GetMapping("/period")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getByPeriod(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getPayrollsByMonthAndYear(month, year), "OK"));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PayrollResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam PayrollStatus status) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.updatePayrollStatus(id, status), "Cập nhật trạng thái thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        payrollService.deletePayroll(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa thành công"));
    }
}
