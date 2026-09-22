package com.kltn.school_hrm.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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
import com.kltn.school_hrm.dto.request.AttendanceCorrectionRequest;
import com.kltn.school_hrm.dto.request.CheckInRequest;
import com.kltn.school_hrm.dto.request.CheckOutRequest;
import com.kltn.school_hrm.dto.response.AttendanceResponse;
import com.kltn.school_hrm.service.AttendanceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /** POST /api/v1/attendances/check-in — Ghi nhận vào ca */
    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn(@Valid @RequestBody CheckInRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(attendanceService.checkIn(request), "Check-in thành công"));
    }

    /** POST /api/v1/attendances/check-out — Ghi nhận tan ca */
    @PostMapping("/check-out")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkOut(@Valid @RequestBody CheckOutRequest request) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.checkOut(request), "Check-out thành công"));
    }

    /** PUT /api/v1/attendances/{id}/correct — Hiệu chỉnh thông tin chấm công */
    @PutMapping("/{id}/correct")
    public ResponseEntity<ApiResponse<AttendanceResponse>> correctAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceCorrectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.correctAttendance(id, request), "Hiệu chỉnh chấm công thành công"));
    }

    /** GET /api/v1/attendances/{id} — Lấy chi tiết chấm công theo ID */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getAttendanceById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAttendanceById(id), "Lấy thông tin chấm công thành công"));
    }

    /** GET /api/v1/attendances — Lấy danh sách tất cả chấm công */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAllAttendances() {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAllAttendances(), "Lấy danh sách chấm công thành công"));
    }

    /** GET /api/v1/attendances/employee/{employeeId} — Lấy lịch sử chấm công của nhân viên */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendancesByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAttendancesByEmployeeId(employeeId), "Lấy danh sách chấm công của nhân viên thành công"));
    }

    /** GET /api/v1/attendances/date — Lấy danh sách chấm công theo ngày làm việc */
    @GetMapping("/date")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendancesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAttendancesByDate(workDate), "Lấy danh sách chấm công theo ngày thành công"));
    }

    /** DELETE /api/v1/attendances/{id} — Xóa bản ghi chấm công */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa bản ghi chấm công thành công"));
    }
}
