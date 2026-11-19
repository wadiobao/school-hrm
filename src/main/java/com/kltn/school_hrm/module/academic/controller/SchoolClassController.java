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
import com.kltn.school_hrm.module.academic.dto.request.SchoolClassRequest;
import com.kltn.school_hrm.module.academic.dto.response.SchoolClassResponse;
import com.kltn.school_hrm.module.academic.service.SchoolClassService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller xử lý các yêu cầu liên quan đến Lớp học.
 */
@RestController
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
public class SchoolClassController {

    private final SchoolClassService schoolClassService;

    @PostMapping
    public ResponseEntity<ApiResponse<SchoolClassResponse>> create(@Valid @RequestBody SchoolClassRequest request) {
        SchoolClassResponse response = schoolClassService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tạo lớp học thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SchoolClassResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SchoolClassRequest request) {
        SchoolClassResponse response = schoolClassService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật lớp học thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SchoolClassResponse>> getById(@PathVariable Long id) {
        SchoolClassResponse response = schoolClassService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin lớp học thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SchoolClassResponse>>> getClasses(
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) Integer gradeLevel) {
        List<SchoolClassResponse> response;
        if (academicYearId != null && gradeLevel != null) {
            response = schoolClassService.getByAcademicYearIdAndGradeLevel(academicYearId, gradeLevel);
        } else if (academicYearId != null) {
            response = schoolClassService.getByAcademicYearId(academicYearId);
        } else if (gradeLevel != null) {
            response = schoolClassService.getAll().stream()
                    .filter(c -> gradeLevel.equals(c.getGradeLevel()))
                    .toList();
        } else {
            response = schoolClassService.getAll();
        }
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách lớp học thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        schoolClassService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa lớp học thành công"));
    }
}

