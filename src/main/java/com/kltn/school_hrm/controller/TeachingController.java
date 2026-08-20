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
import com.kltn.school_hrm.dto.request.TeachingLogRequest;
import com.kltn.school_hrm.dto.request.TeachingNormRequest;
import com.kltn.school_hrm.dto.response.TeachingLogResponse;
import com.kltn.school_hrm.dto.response.TeachingNormResponse;
import com.kltn.school_hrm.enums.Enums.RequestStatus;
import com.kltn.school_hrm.service.TeachingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TeachingController {

    private final TeachingService teachingService;

    // ===================== Teaching Log =====================

    @PostMapping("/api/v1/teaching-logs")
    public ResponseEntity<ApiResponse<TeachingLogResponse>> createLog(
            @Valid @RequestBody TeachingLogRequest request) {
        TeachingLogResponse response = teachingService.createLog(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tạo nhật ký giảng dạy thành công"));
    }

    @PutMapping("/api/v1/teaching-logs/{id}")
    public ResponseEntity<ApiResponse<TeachingLogResponse>> updateLog(
            @PathVariable Long id,
            @Valid @RequestBody TeachingLogRequest request) {
        TeachingLogResponse response = teachingService.updateLog(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật nhật ký giảng dạy thành công"));
    }

    @GetMapping("/api/v1/teaching-logs/{id}")
    public ResponseEntity<ApiResponse<TeachingLogResponse>> getLogById(@PathVariable Long id) {
        TeachingLogResponse response = teachingService.getLogById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy nhật ký giảng dạy thành công"));
    }

    @GetMapping("/api/v1/teaching-logs")
    public ResponseEntity<ApiResponse<List<TeachingLogResponse>>> getAllLogs(
            @RequestParam(required = false) Long assignmentId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) RequestStatus status) {
        List<TeachingLogResponse> response;
        if (assignmentId != null) {
            response = teachingService.getLogsByAssignmentId(assignmentId);
        } else if (teacherId != null) {
            response = teachingService.getLogsByTeacherId(teacherId);
        } else if (status != null) {
            response = teachingService.getLogsByStatus(status);
        } else {
            response = teachingService.getAllLogs();
        }
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách nhật ký giảng dạy thành công"));
    }

    @DeleteMapping("/api/v1/teaching-logs/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLog(@PathVariable Long id) {
        teachingService.deleteLog(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa nhật ký giảng dạy thành công"));
    }

    // ===================== Teaching Norm =====================

    @PostMapping("/api/v1/teaching-norms")
    public ResponseEntity<ApiResponse<TeachingNormResponse>> createNorm(
            @Valid @RequestBody TeachingNormRequest request) {
        TeachingNormResponse response = teachingService.createNorm(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tạo định mức giảng dạy thành công"));
    }

    @PutMapping("/api/v1/teaching-norms/{id}")
    public ResponseEntity<ApiResponse<TeachingNormResponse>> updateNorm(
            @PathVariable Long id,
            @Valid @RequestBody TeachingNormRequest request) {
        TeachingNormResponse response = teachingService.updateNorm(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật định mức giảng dạy thành công"));
    }

    @GetMapping("/api/v1/teaching-norms/{id}")
    public ResponseEntity<ApiResponse<TeachingNormResponse>> getNormById(@PathVariable Long id) {
        TeachingNormResponse response = teachingService.getNormById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy định mức giảng dạy thành công"));
    }

    @GetMapping("/api/v1/teaching-norms")
    public ResponseEntity<ApiResponse<List<TeachingNormResponse>>> getAllNorms(
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) Long positionId) {
        List<TeachingNormResponse> response;
        if (academicYear != null) {
            response = teachingService.getNormsByAcademicYear(academicYear);
        } else if (positionId != null) {
            response = teachingService.getNormsByPositionId(positionId);
        } else {
            response = teachingService.getAllNorms();
        }
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách định mức giảng dạy thành công"));
    }

    @DeleteMapping("/api/v1/teaching-norms/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNorm(@PathVariable Long id) {
        teachingService.deleteNorm(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa định mức giảng dạy thành công"));
    }
}
