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
import org.springframework.web.bind.annotation.RestController;

import com.kltn.school_hrm.dto.common.ApiResponse;
import com.kltn.school_hrm.module.academic.dto.request.AcademicYearRequest;
import com.kltn.school_hrm.module.academic.dto.response.AcademicYearResponse;
import com.kltn.school_hrm.module.academic.service.AcademicYearService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller xử lý các yêu cầu liên quan đến Năm học.
 */
@RestController
@RequestMapping("/api/v1/academic-years")
@RequiredArgsConstructor
public class AcademicYearController {

    private final AcademicYearService academicYearService;

    @PostMapping
    public ResponseEntity<ApiResponse<AcademicYearResponse>> create(@Valid @RequestBody AcademicYearRequest request) {
        AcademicYearResponse response = academicYearService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tạo năm học thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AcademicYearResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody AcademicYearRequest request) {
        AcademicYearResponse response = academicYearService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật năm học thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AcademicYearResponse>> getById(@PathVariable Long id) {
        AcademicYearResponse response = academicYearService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin năm học thành công"));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<AcademicYearResponse>> getActiveYear() {
        AcademicYearResponse response = academicYearService.getActiveYear();
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy năm học đang hoạt động thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AcademicYearResponse>>> getAll() {
        List<AcademicYearResponse> response = academicYearService.getAll();
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách năm học thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        academicYearService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa năm học thành công"));
    }
}

