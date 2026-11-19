package com.kltn.school_hrm.module.scheduling.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kltn.school_hrm.dto.common.ApiResponse;
import com.kltn.school_hrm.module.scheduling.constraint.ValidationResult;
import com.kltn.school_hrm.module.scheduling.dto.request.AutoScheduleSettingRequest;
import com.kltn.school_hrm.module.scheduling.dto.request.TimetableRequest;
import com.kltn.school_hrm.module.scheduling.dto.response.AutoScheduleResultResponse;
import com.kltn.school_hrm.module.scheduling.dto.response.TeacherTimetableResponse;
import com.kltn.school_hrm.module.scheduling.dto.response.TimetableMatrixResponse;
import com.kltn.school_hrm.module.scheduling.dto.response.TimetableResponse;
import com.kltn.school_hrm.module.scheduling.service.AutoScheduleEngineService;
import com.kltn.school_hrm.module.scheduling.service.TimetableService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller quản lý vòng đời và công bố thời khóa biểu.
 */
@RestController
@RequestMapping("/api/v1/timetables")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;
    private final AutoScheduleEngineService autoScheduleEngineService;

    @PostMapping("/auto-generate")
    @PreAuthorize("@schedulingSecurity.canManageSchedule()")
    public ResponseEntity<ApiResponse<AutoScheduleResultResponse>> autoGenerate(
            @Valid @RequestBody AutoScheduleSettingRequest request) {
        AutoScheduleResultResponse result = autoScheduleEngineService.generateSemesterSchedule(request);
        return ResponseEntity.ok(ApiResponse.success(result,
                String.format("Đã tự động phân công giáo viên và xếp thời khóa biểu cho %d lớp (%d tiết) trong %d ms",
                        result.getTotalClassesScheduled(), result.getTotalPeriodsScheduled(), result.getExecutionTimeMs())));
    }

    @PostMapping
    @PreAuthorize("@schedulingSecurity.canManageSchedule()")
    public ResponseEntity<ApiResponse<TimetableResponse>> create(@Valid @RequestBody TimetableRequest request) {
        TimetableResponse response = timetableService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Khởi tạo thời khóa biểu (Bản nháp) thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TimetableResponse>> getById(@PathVariable Long id) {
        TimetableResponse response = timetableService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin thời khóa biểu thành công"));
    }

    @GetMapping("/{id}/matrix")
    public ResponseEntity<ApiResponse<TimetableMatrixResponse>> getMatrix(@PathVariable Long id) {
        TimetableMatrixResponse response = timetableService.getMatrix(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy ma trận thời khóa biểu tuần thành công"));
    }

    @GetMapping("/teacher-matrix")
    public ResponseEntity<ApiResponse<TeacherTimetableResponse>> getTeacherMatrix(
            @RequestParam Long semesterId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long departmentId) {
        TeacherTimetableResponse response = timetableService.getTeacherTimetable(semesterId, teacherId, departmentId);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thời khóa biểu theo giáo viên thành công"));
    }

    @GetMapping("/published")
    public ResponseEntity<ApiResponse<TimetableResponse>> getPublished(
            @RequestParam Long schoolClassId,
            @RequestParam Long semesterId) {
        TimetableResponse response = timetableService.getPublishedTimetable(schoolClassId, semesterId);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thời khóa biểu đang áp dụng thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TimetableResponse>>> getVersions(
            @RequestParam Long schoolClassId,
            @RequestParam Long semesterId) {
        List<TimetableResponse> response = timetableService.getByClassAndSemester(schoolClassId, semesterId);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách các phiên bản thời khóa biểu thành công"));
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<ApiResponse<ValidationResult>> validate(@PathVariable Long id) {
        ValidationResult result = timetableService.validateTimetable(id);
        String msg = result.isValid() ? "Kiểm tra hợp lệ thành công. Đã chuyển sang trạng thái CHỜ DUYỆT" :
                "Kiểm tra phát hiện lỗi xung đột ràng buộc";
        return ResponseEntity.ok(ApiResponse.success(result, msg));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("@schedulingSecurity.canManageSchedule()")
    public ResponseEntity<ApiResponse<TimetableResponse>> publish(@PathVariable Long id) {
        TimetableResponse response = timetableService.publish(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Công bố thời khóa biểu thành công"));
    }

    @PostMapping("/{id}/new-version")
    @PreAuthorize("@schedulingSecurity.canManageSchedule()")
    public ResponseEntity<ApiResponse<TimetableResponse>> cloneNewVersion(@PathVariable Long id) {
        TimetableResponse response = timetableService.createNewVersionFromExisting(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tạo phiên bản mới từ thời khóa biểu hiện tại thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@schedulingSecurity.canManageSchedule()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        timetableService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa thời khóa biểu thành công"));
    }
}

