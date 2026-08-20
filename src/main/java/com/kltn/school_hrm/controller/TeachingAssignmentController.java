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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kltn.school_hrm.dto.common.ApiResponse;
import com.kltn.school_hrm.dto.request.TeachingAssignmentRequest;
import com.kltn.school_hrm.dto.response.TeachingAssignmentResponse;
import com.kltn.school_hrm.enums.Enums.Curriculum;
import com.kltn.school_hrm.service.TeachingAssignmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/teaching-assignments")
@RequiredArgsConstructor
public class TeachingAssignmentController {

    private final TeachingAssignmentService teachingAssignmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<TeachingAssignmentResponse>> create(
            @Valid @RequestBody TeachingAssignmentRequest request) {
        TeachingAssignmentResponse response = teachingAssignmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tạo phân công giảng dạy thành công"));
    }

    @PutMapping("/{id}")
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

    @GetMapping
    public ResponseEntity<ApiResponse<List<TeachingAssignmentResponse>>> getAll() {
        List<TeachingAssignmentResponse> response = teachingAssignmentService.getAll();
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách phân công thành công"));
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<ApiResponse<List<TeachingAssignmentResponse>>> getByTeacher(
            @PathVariable Long teacherId,
            @RequestParam(required = false) Curriculum curriculum) {
        List<TeachingAssignmentResponse> response = curriculum != null
                ? teachingAssignmentService.getByTeacherIdAndCurriculum(teacherId, curriculum)
                : teachingAssignmentService.getByTeacherId(teacherId);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách phân công theo giáo viên thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        teachingAssignmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa phân công giảng dạy thành công"));
    }
}
