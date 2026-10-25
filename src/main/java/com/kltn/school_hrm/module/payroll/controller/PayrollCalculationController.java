package com.kltn.school_hrm.module.payroll.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kltn.school_hrm.dto.common.ApiResponse;
import com.kltn.school_hrm.module.payroll.dto.response.PayrollResponse;
import com.kltn.school_hrm.module.payroll.service.PayrollCalculationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payroll-calc")
@RequiredArgsConstructor
public class PayrollCalculationController {

    private final PayrollCalculationService calcService;

    /** Tính lương tự động cho tất cả nhân viên trong kỳ */
    @PostMapping("/period/{periodId}/calculate")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> calculateForPeriod(
            @PathVariable Long periodId) {
        List<PayrollResponse> results = calcService.calculateForPeriod(periodId);
        return ResponseEntity.ok(ApiResponse.success(results,
                "Đã tính lương cho " + results.size() + " nhân viên"));
    }

    /** Tính (lại) lương cho một nhân viên cụ thể */
    @PostMapping("/period/{periodId}/employee/{employeeId}")
    public ResponseEntity<ApiResponse<PayrollResponse>> calculateForEmployee(
            @PathVariable Long periodId,
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success(
                calcService.calculateForEmployee(periodId, employeeId),
                "Tính lương thành công"));
    }
}
