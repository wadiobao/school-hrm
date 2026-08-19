package com.kltn.school_hrm.controller;

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
import com.kltn.school_hrm.dto.request.PayrollCreateRequest;
import com.kltn.school_hrm.dto.response.PayrollResponse;
import com.kltn.school_hrm.enums.Enums.PayrollStatus;
import com.kltn.school_hrm.service.PayrollService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payrolls")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping
    public ResponseEntity<ApiResponse<PayrollResponse>> createPayroll(@Valid @RequestBody PayrollCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(payrollService.createPayroll(request), "Payroll created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PayrollResponse>> updatePayroll(@PathVariable Long id, @Valid @RequestBody PayrollCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.updatePayroll(id, request), "Payroll updated successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PayrollResponse>> getPayrollById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getPayrollById(id), "Payroll retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getAllPayrolls() {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getAllPayrolls(), "Payrolls retrieved successfully"));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getPayrollsByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getPayrollsByEmployeeId(employeeId), "Payrolls retrieved successfully"));
    }

    @GetMapping("/period")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getPayrollsByPeriod(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getPayrollsByMonthAndYear(month, year), "Payrolls retrieved successfully"));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PayrollResponse>> updatePayrollStatus(
            @PathVariable Long id,
            @RequestParam PayrollStatus status) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.updatePayrollStatus(id, status), "Payroll status updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePayroll(@PathVariable Long id) {
        payrollService.deletePayroll(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Payroll deleted successfully"));
    }
}
