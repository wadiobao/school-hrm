package com.kltn.school_hrm.module.formula.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kltn.school_hrm.dto.common.ApiResponse;
import com.kltn.school_hrm.module.formula.dto.request.FormulaDefinitionRequest;
import com.kltn.school_hrm.module.formula.dto.request.FormulaEvaluateRequest;
import com.kltn.school_hrm.module.formula.dto.request.FormulaValidateRequest;
import com.kltn.school_hrm.module.formula.dto.response.FormulaDefinitionResponse;
import com.kltn.school_hrm.module.formula.dto.response.FormulaEvaluateResponse;
import com.kltn.school_hrm.module.formula.dto.response.FormulaValidateResponse;
import com.kltn.school_hrm.module.formula.dto.response.FormulaVariableResponse;
import com.kltn.school_hrm.module.formula.service.FormulaDefinitionService;
import com.kltn.school_hrm.module.formula.service.FormulaEngineService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/formulas")
@RequiredArgsConstructor
public class FormulaDefinitionController {

    private final FormulaDefinitionService formulaDefinitionService;
    private final FormulaEngineService formulaEngineService;

    @GetMapping("/variables")
    public ResponseEntity<ApiResponse<List<FormulaVariableResponse>>> getVariables() {
        return ResponseEntity.ok(ApiResponse.success(formulaDefinitionService.getVariables(),
                "Lấy danh sách biến công thức thành công"));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<FormulaValidateResponse>> validate(
            @Valid @RequestBody FormulaValidateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(formulaEngineService.validate(request.getExpression()),
                "Kiểm tra công thức thành công"));
    }

    @PostMapping("/evaluate")
    public ResponseEntity<ApiResponse<FormulaEvaluateResponse>> evaluate(
            @Valid @RequestBody FormulaEvaluateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                formulaEngineService.evaluate(request.getExpression(), request.getVariables()),
                "Tính thử công thức thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FormulaDefinitionResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(formulaDefinitionService.getAll(),
                "Lấy danh sách công thức thành công"));
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<ApiResponse<FormulaDefinitionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(formulaDefinitionService.getById(id),
                "Lấy chi tiết công thức thành công"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('HR_MANAGER') or hasRole('HR_MANAGER')")
    public ResponseEntity<ApiResponse<FormulaDefinitionResponse>> create(
            @Valid @RequestBody FormulaDefinitionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(formulaDefinitionService.create(request), "Tạo công thức thành công"));
    }

    @PutMapping("/{id:[0-9]+}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('HR_MANAGER') or hasRole('HR_MANAGER')")
    public ResponseEntity<ApiResponse<FormulaDefinitionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody FormulaDefinitionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(formulaDefinitionService.update(id, request),
                "Cập nhật công thức thành công"));
    }

    @DeleteMapping("/{id:[0-9]+}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        formulaDefinitionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa công thức thành công"));
    }
}
