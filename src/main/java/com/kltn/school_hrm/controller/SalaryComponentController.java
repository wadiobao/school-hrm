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
import org.springframework.web.bind.annotation.RestController;

import com.kltn.school_hrm.dto.common.ApiResponse;
import com.kltn.school_hrm.dto.request.SalaryComponentRequest;
import com.kltn.school_hrm.dto.response.SalaryComponentResponse;
import com.kltn.school_hrm.service.SalaryComponentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/salary-components")
@RequiredArgsConstructor
public class SalaryComponentController {

    private final SalaryComponentService salaryComponentService;

    @PostMapping
    public ResponseEntity<ApiResponse<SalaryComponentResponse>> create(@Valid @RequestBody SalaryComponentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(salaryComponentService.create(request), "Salary component created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SalaryComponentResponse>> update(@PathVariable Long id, @Valid @RequestBody SalaryComponentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(salaryComponentService.update(id, request), "Salary component updated successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SalaryComponentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(salaryComponentService.getById(id), "Salary component retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SalaryComponentResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(salaryComponentService.getAll(), "Salary components retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        salaryComponentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Salary component deleted successfully"));
    }
}
