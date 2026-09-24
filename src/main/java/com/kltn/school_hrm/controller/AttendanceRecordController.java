package com.kltn.school_hrm.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kltn.school_hrm.dto.response.AttendanceRecordResponse;
import com.kltn.school_hrm.service.AttendanceRecordService;

import lombok.RequiredArgsConstructor;

/**
 * Cho HR/User xem attendance records đã được aggregate.
 *
 * Theo req.md section 11:
 *   GET /api/attendance-records/{id}
 *   GET /api/employees/{employeeId}/attendance-records
 *   GET /api/attendance-records?from=2026-09-01&to=2026-09-30
 */
@RestController
@RequiredArgsConstructor
public class AttendanceRecordController {

    private final AttendanceRecordService recordService;

    /**
     * GET /api/attendance-records/{id}
     */
    @GetMapping("/api/attendance-records/{id}")
    public ResponseEntity<AttendanceRecordResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(recordService.getById(id));
    }

    /**
     * GET /api/attendance-records?from=2026-09-01&to=2026-09-30
     */
    @GetMapping("/api/attendance-records")
    public ResponseEntity<List<AttendanceRecordResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(recordService.getByDateRange(from, to));
    }

    /**
     * GET /api/employees/{employeeId}/attendance-records
     */
    @GetMapping("/api/employees/{employeeId}/attendance-records")
    public ResponseEntity<List<AttendanceRecordResponse>> getByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        if (from != null && to != null) {
            return ResponseEntity.ok(recordService.getByEmployeeAndDateRange(employeeId, from, to));
        }
        return ResponseEntity.ok(recordService.getByEmployee(employeeId));
    }

    /**
     * HR trigger re-aggregate thủ công (chạy lại Aggregation từ raw logs).
     * POST /api/attendance-records/re-aggregate?employeeId=15&workDate=2026-09-23
     */
    @PostMapping("/api/attendance-records/re-aggregate")
    public ResponseEntity<AttendanceRecordResponse> reAggregate(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate) {
        return ResponseEntity.ok(recordService.reAggregate(employeeId, workDate));
    }
}
