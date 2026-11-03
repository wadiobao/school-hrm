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
import com.kltn.school_hrm.module.academic.dto.request.ClassSubjectRequest;
import com.kltn.school_hrm.module.academic.dto.response.ClassSubjectResponse;
import com.kltn.school_hrm.module.academic.service.ClassSubjectService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller quản lý phân phối chương trình môn học theo lớp và học kỳ.
 */
@RestController
@RequestMapping("/api/v1/class-subjects")
@RequiredArgsConstructor
public class ClassSubjectController {

    private final ClassSubjectService classSubjectService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClassSubjectResponse>> create(@Valid @RequestBody ClassSubjectRequest request) {
        ClassSubjectResponse response = classSubjectService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Cấu hình môn học cho lớp thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassSubjectResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ClassSubjectRequest request) {
        ClassSubjectResponse response = classSubjectService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật phân phối môn học thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassSubjectResponse>> getById(@PathVariable Long id) {
        ClassSubjectResponse response = classSubjectService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin môn học của lớp thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClassSubjectResponse>>> search(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long semesterId) {
        List<ClassSubjectResponse> response;
        if (classId != null && semesterId != null) {
            response = classSubjectService.getByClassIdAndSemesterId(classId, semesterId);
        } else if (classId != null) {
            response = classSubjectService.getByClassId(classId);
        } else if (semesterId != null) {
            response = classSubjectService.getBySemesterId(semesterId);
        } else {
            response = List.of();
        }
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách môn học theo lớp/học kỳ thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        classSubjectService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa phân phối môn học thành công"));
    }
}

