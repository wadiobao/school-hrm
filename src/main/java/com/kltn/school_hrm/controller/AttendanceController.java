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
import com.kltn.school_hrm.dto.request.AttendanceCreateRequest;
import com.kltn.school_hrm.dto.response.AttendanceResponse;
import com.kltn.school_hrm.service.AttendanceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    public ResponseEntity<ApiResponse<AttendanceResponse>> createAttendance(@Valid @RequestBody AttendanceCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(attendanceService.createAttendance(request), "Attendance created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> updateAttendance(@PathVariable Long id, @Valid @RequestBody AttendanceCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.updateAttendance(id, request), "Attendance updated successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getAttendanceById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAttendanceById(id), "Attendance retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAllAttendances() {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAllAttendances(), "Attendances retrieved successfully"));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendancesByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAttendancesByEmployeeId(employeeId), "Attendances retrieved successfully"));
    }

    @GetMapping("/date")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendancesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAttendancesByDate(workDate), "Attendances retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Attendance deleted successfully"));
    }
}
