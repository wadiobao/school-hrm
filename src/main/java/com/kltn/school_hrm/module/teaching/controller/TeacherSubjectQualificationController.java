package com.kltn.school_hrm.module.teaching.controller;

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
import com.kltn.school_hrm.module.teaching.dto.request.TeacherSubjectQualificationRequest;
import com.kltn.school_hrm.module.teaching.dto.response.TeacherSubjectQualificationResponse;
import com.kltn.school_hrm.module.teaching.service.TeacherSubjectQualificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller quản lý chuyên môn giảng dạy của giáo viên theo môn học.
 */
@RestController
@RequestMapping("/api/v1/teacher-qualifications")
@RequiredArgsConstructor
public class TeacherSubjectQualificationController {

    private final TeacherSubjectQualificationService qualificationService;

    @PostMapping
    public ResponseEntity<ApiResponse<TeacherSubjectQualificationResponse>> create(
            @Valid @RequestBody TeacherSubjectQualificationRequest request) {
        TeacherSubjectQualificationResponse response = qualificationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Khai báo chuyên môn giáo viên thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TeacherSubjectQualificationResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody TeacherSubjectQualificationRequest request) {
        TeacherSubjectQualificationResponse response = qualificationService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật chuyên môn giáo viên thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeacherSubjectQualificationResponse>> getById(@PathVariable Long id) {
        TeacherSubjectQualificationResponse response = qualificationService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin chuyên môn thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TeacherSubjectQualificationResponse>>> search(
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long subjectId) {
        List<TeacherSubjectQualificationResponse> response;
        if (teacherId != null) {
            response = qualificationService.getByTeacherId(teacherId);
        } else if (subjectId != null) {
            response = qualificationService.getBySubjectId(subjectId);
        } else {
            response = List.of();
        }
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách chuyên môn thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        qualificationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa chuyên môn giáo viên thành công"));
    }
}

