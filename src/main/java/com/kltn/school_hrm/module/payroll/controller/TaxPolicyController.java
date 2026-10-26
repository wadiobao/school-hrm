package com.kltn.school_hrm.module.payroll.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kltn.school_hrm.dto.common.ApiResponse;
import com.kltn.school_hrm.module.payroll.dto.request.TaxPolicyRequest;
import com.kltn.school_hrm.module.payroll.dto.response.TaxPolicyResponse;
import com.kltn.school_hrm.module.payroll.service.TaxPolicyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller quản lý cấu hình chính sách thuế TNCN, các định mức giảm trừ và bậc thuế.
 * Quy định bảo mật: Chỉ SUPER_ADMIN mới có quyền cập nhật và tạo mới chính sách thuế.
 */
@RestController
@RequestMapping("/api/v1/tax-policies")
@RequiredArgsConstructor
public class TaxPolicyController {

    private final TaxPolicyService taxPolicyService;

    /**
     * Lấy chính sách thuế TNCN đang áp dụng hiện tại (hoặc theo ngày chỉ định).
     * Hỗ trợ cả /active và /current.
     */
    @GetMapping({"/active", "/current"})
    public ResponseEntity<ApiResponse<TaxPolicyResponse>> getActivePolicy(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        TaxPolicyResponse policy = taxPolicyService.getCurrentPolicy(date);
        return ResponseEntity.ok(ApiResponse.success(policy, "Lấy chính sách thuế hiện hành thành công"));
    }

    /**
     * Lấy danh sách toàn bộ các chính sách thuế.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaxPolicyResponse>>> getAllPolicies() {
        return ResponseEntity.ok(ApiResponse.success(taxPolicyService.getAllPolicies(), "Lấy danh sách chính sách thuế thành công"));
    }

    /**
     * Lấy chi tiết chính sách thuế theo ID.
     */
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<ApiResponse<TaxPolicyResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(taxPolicyService.getPolicyById(id), "Lấy chi tiết chính sách thuế thành công"));
    }

    /**
     * Cập nhật chính sách thuế: mức giảm trừ bản thân, người phụ thuộc, tỷ lệ bảo hiểm và các bậc thuế.
     * BẢO MẬT: CHỈ SUPER_ADMIN CÓ QUYỀN SỬA.
     */
    @PutMapping("/{id:[0-9]+}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TaxPolicyResponse>> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody TaxPolicyRequest request) {
        TaxPolicyResponse updated = taxPolicyService.updatePolicy(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật chính sách thuế thành công"));
    }

    /**
     * Tạo mới một chính sách thuế.
     * BẢO MẬT: CHỈ SUPER_ADMIN CÓ QUYỀN TẠO.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TaxPolicyResponse>> createPolicy(
            @Valid @RequestBody TaxPolicyRequest request) {
        TaxPolicyResponse created = taxPolicyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Tạo mới chính sách thuế thành công"));
    }
}
