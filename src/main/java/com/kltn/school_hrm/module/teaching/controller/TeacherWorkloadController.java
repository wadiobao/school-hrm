package com.kltn.school_hrm.module.teaching.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kltn.school_hrm.dto.common.ApiResponse;
import com.kltn.school_hrm.module.teaching.dto.response.TeacherWorkloadResponse;
import com.kltn.school_hrm.module.teaching.service.TeacherWorkloadService;

import lombok.RequiredArgsConstructor;

/**
 * Controller theo dõi tải giảng dạy và cân đối tiết học của giáo viên.
 */
@RestController
@RequestMapping("/api/v1/teacher-workloads")
@RequiredArgsConstructor
public class TeacherWorkloadController {

    private final TeacherWorkloadService teacherWorkloadService;

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<ApiResponse<TeacherWorkloadResponse>> getTeacherWorkload(
            @PathVariable Long teacherId,
            @RequestParam Long semesterId) {
        TeacherWorkloadResponse response = teacherWorkloadService.getTeacherWorkload(teacherId, semesterId);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin tải giảng dạy của giáo viên thành công"));
    }

    @GetMapping("/semester/{semesterId}")
    public ResponseEntity<ApiResponse<List<TeacherWorkloadResponse>>> getSemesterWorkloads(
            @PathVariable Long semesterId) {
        List<TeacherWorkloadResponse> response = teacherWorkloadService.getWorkloadsBySemester(semesterId);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách tải giảng dạy theo học kỳ thành công"));
    }
}

