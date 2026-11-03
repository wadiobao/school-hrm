package com.kltn.school_hrm.module.academic.controller;

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
import com.kltn.school_hrm.module.academic.dto.request.SubjectRequest;
import com.kltn.school_hrm.module.academic.dto.response.SubjectResponse;
import com.kltn.school_hrm.module.academic.service.SubjectService;
import com.kltn.school_hrm.shared.enums.Enums.SubjectStatus;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller xử lý các yêu cầu liên quan đến Môn học.
 */
@RestController
@RequestMapping("/api/v1/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    public ResponseEntity<ApiResponse<SubjectResponse>> create(@Valid @RequestBody SubjectRequest request) {
        SubjectResponse response = subjectService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tạo môn học thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubjectResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SubjectRequest request) {
        SubjectResponse response = subjectService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật môn học thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubjectResponse>> getById(@PathVariable Long id) {
        SubjectResponse response = subjectService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin môn học thành công"));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<SubjectResponse>> getByCode(@PathVariable String code) {
        SubjectResponse response = subjectService.getByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin môn học thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getAll(
            @RequestParam(required = false) SubjectStatus status) {
        List<SubjectResponse> response;
        if (status != null) {
            response = subjectService.getByStatus(status);
        } else {
            response = subjectService.getAll();
        }
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách môn học thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        subjectService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa môn học thành công"));
    }
}

