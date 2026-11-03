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
import com.kltn.school_hrm.module.academic.dto.request.SemesterRequest;
import com.kltn.school_hrm.module.academic.dto.response.SemesterResponse;
import com.kltn.school_hrm.module.academic.service.SemesterService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller xử lý các yêu cầu liên quan đến Học kỳ.
 */
@RestController
@RequestMapping("/api/v1/semesters")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterService semesterService;

    @PostMapping
    public ResponseEntity<ApiResponse<SemesterResponse>> create(@Valid @RequestBody SemesterRequest request) {
        SemesterResponse response = semesterService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tạo học kỳ thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SemesterResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SemesterRequest request) {
        SemesterResponse response = semesterService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật học kỳ thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SemesterResponse>> getById(@PathVariable Long id) {
        SemesterResponse response = semesterService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin học kỳ thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SemesterResponse>>> getByAcademicYearId(
            @RequestParam Long academicYearId) {
        List<SemesterResponse> response = semesterService.getByAcademicYearId(academicYearId);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách học kỳ thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        semesterService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa học kỳ thành công"));
    }
}

