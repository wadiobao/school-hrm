package com.kltn.school_hrm.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kltn.school_hrm.dto.request.AttendanceRawLogRequest;
import com.kltn.school_hrm.dto.response.AttendanceRawLogResponse;
import com.kltn.school_hrm.service.AttendanceRawLogService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Nhận raw attendance events từ mọi nguồn bên ngoài.
 * Controller chỉ tiếp nhận request, không chứa bất kỳ business logic nào.
 *
 * Theo req.md section 9: POST /api/attendance/raw-logs
 */
@RestController
@RequestMapping("/api/attendance/raw-logs")
@RequiredArgsConstructor
public class AttendanceRawLogController {

    private final AttendanceRawLogService rawLogService;

    /**
     * Ghi nhận raw attendance event (check-in hoặc check-out).
     *
     * <pre>
     * POST /api/attendance/raw-logs
     * {
     *   "employeeId": 15,
     *   "eventTime": "2026-09-23T07:58:00",
     *   "eventType": "IN",
     *   "source": "WEB"
     * }
     * </pre>
     */
    @PostMapping
    public ResponseEntity<AttendanceRawLogResponse> recordEvent(
            @Valid @RequestBody AttendanceRawLogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rawLogService.recordEvent(request));
    }

    /**
     * Lấy raw logs của nhân viên (dành cho debug/audit trail).
     * GET /api/attendance/raw-logs/employees/{employeeId}
     */
    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<List<AttendanceRawLogResponse>> getLogsByEmployee(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(rawLogService.getLogsByEmployee(employeeId));
    }
}
