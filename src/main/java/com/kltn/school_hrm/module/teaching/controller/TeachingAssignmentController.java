package com.kltn.school_hrm.module.teaching.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kltn.school_hrm.dto.common.ApiResponse;
import com.kltn.school_hrm.module.teaching.dto.request.TeachingAssignmentRequest;
import com.kltn.school_hrm.module.teaching.dto.response.TeachingAssignmentHistoryResponse;
import com.kltn.school_hrm.module.teaching.dto.response.TeachingAssignmentResponse;
import com.kltn.school_hrm.module.teaching.service.TeachingAssignmentService;
import com.kltn.school_hrm.shared.enums.Enums.Curriculum;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller xử lý các yêu cầu liên quan đến Phân công giảng dạy.
 */
@RestController
@RequestMapping("/api/v1/teaching-assignments")
@RequiredArgsConstructor
public class TeachingAssignmentController {

    private final TeachingAssignmentService teachingAssignmentService;

    @PostMapping
    @PreAuthorize("@schedulingSecurity.canManageSchedule()")
    public ResponseEntity<ApiResponse<TeachingAssignmentResponse>> create(
            @Valid @RequestBody TeachingAssignmentRequest request) {
        TeachingAssignmentResponse response = teachingAssignmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tạo phân công giảng dạy thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@schedulingSecurity.canManageSchedule()")
    public ResponseEntity<ApiResponse<TeachingAssignmentResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody TeachingAssignmentRequest request) {
        TeachingAssignmentResponse response = teachingAssignmentService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật phân công giảng dạy thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeachingAssignmentResponse>> getById(@PathVariable Long id) {
        TeachingAssignmentResponse response = teachingAssignmentService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin phân công thành công"));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<TeachingAssignmentHistoryResponse>>> getHistory(@PathVariable Long id) {
        List<TeachingAssignmentHistoryResponse> response = teachingAssignmentService.getAssignmentHistory(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy lịch sử phân công thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TeachingAssignmentResponse>>> getAll(
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Curriculum curriculum) {
        List<TeachingAssignmentResponse> response;
        if (classId != null) {
            response = teachingAssignmentService.getBySchoolClassId(classId);
        } else if (teacherId != null && curriculum != null) {
            response = teachingAssignmentService.getByTeacherIdAndCurriculum(teacherId, curriculum);
        } else if (teacherId != null) {
            response = teachingAssignmentService.getByTeacherId(teacherId);
        } else {
            response = teachingAssignmentService.getAll();
        }
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách phân công thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@schedulingSecurity.canManageSchedule()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        teachingAssignmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa phân công giảng dạy thành công"));
    }
}
