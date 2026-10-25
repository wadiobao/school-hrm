package com.kltn.school_hrm.module.payroll.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kltn.school_hrm.dto.common.ApiResponse;
import com.kltn.school_hrm.module.payroll.dto.request.PayrollPeriodRequest;
import com.kltn.school_hrm.module.payroll.dto.response.PayrollPeriodResponse;
import com.kltn.school_hrm.module.payroll.service.PayrollPeriodService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payroll-periods")
@RequiredArgsConstructor
public class PayrollPeriodController {

    private final PayrollPeriodService periodService;

    @PostMapping
    public ResponseEntity<ApiResponse<PayrollPeriodResponse>> create(
            @Valid @RequestBody PayrollPeriodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(periodService.createPeriod(request), "Tạo kỳ lương thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PayrollPeriodResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(periodService.getPeriodById(id), "OK"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PayrollPeriodResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(periodService.getAllPeriods(), "OK"));
    }

    @PutMapping("/{id}/lock")
    public ResponseEntity<ApiResponse<PayrollPeriodResponse>> lock(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(periodService.lockPeriod(id), "Chốt kỳ lương thành công"));
    }
}
