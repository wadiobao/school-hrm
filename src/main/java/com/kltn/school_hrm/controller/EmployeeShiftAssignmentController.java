package com.kltn.school_hrm.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kltn.school_hrm.dto.common.ApiResponse;
import com.kltn.school_hrm.dto.request.AssignShiftRequest;
import com.kltn.school_hrm.dto.request.ChangeShiftRequest;
import com.kltn.school_hrm.dto.request.EndAssignmentRequest;
import com.kltn.school_hrm.dto.response.EmployeeShiftAssignmentResponse;
import com.kltn.school_hrm.service.EmployeeShiftAssignmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/shift-assignments")
@RequiredArgsConstructor
public class EmployeeShiftAssignmentController {

    private final EmployeeShiftAssignmentService shiftAssignmentService;

    /** POST /api/v1/shift-assignments — Phân công ca làm việc */
    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeShiftAssignmentResponse>> assign(
            @Valid @RequestBody AssignShiftRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(shiftAssignmentService.assign(request), "Phân công ca làm việc thành công"));
    }

    /** PUT /api/v1/shift-assignments/{id}/change-shift — Đổi ca làm việc (đóng ca cũ, tạo ca mới) */
    @PutMapping("/{id}/change-shift")
    public ResponseEntity<ApiResponse<EmployeeShiftAssignmentResponse>> changeShift(
            @PathVariable Long id,
            @Valid @RequestBody ChangeShiftRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(shiftAssignmentService.changeShift(id, request), "Thay đổi ca làm việc thành công"));
    }

    /** PUT /api/v1/shift-assignments/{id}/end — Đóng/kết thúc phân ca tại ngày chỉ định */
    @PutMapping("/{id}/end")
    public ResponseEntity<ApiResponse<Void>> endAssignment(
            @PathVariable Long id,
            @Valid @RequestBody EndAssignmentRequest request) {
        shiftAssignmentService.endAssignment(id, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Kết thúc phân công ca thành công"));
    }

    /** GET /api/v1/shift-assignments/for-date — Tra cứu ca làm việc theo ngày của nhân viên */
    @GetMapping("/for-date")
    public ResponseEntity<ApiResponse<EmployeeShiftAssignmentResponse>> findForDate(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return shiftAssignmentService.findForDate(employeeId, date)
                .map(res -> ResponseEntity.ok(ApiResponse.success(res, "Tìm thấy ca làm việc cho ngày")))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "Không có ca làm việc nào được phân công cho ngày này")));
    }

    /** GET /api/v1/shift-assignments/employee/{employeeId} — Lấy thời khóa biểu/lịch làm việc của nhân viên */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<EmployeeShiftAssignmentResponse>>> getEmployeeSchedule(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                ApiResponse.success(shiftAssignmentService.getEmployeeSchedule(employeeId), "Lấy lịch làm việc của nhân viên thành công"));
    }

    /** GET /api/v1/shift-assignments — Lấy danh sách tất cả các phân công ca */
    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeShiftAssignmentResponse>>> getAllAssignments() {
        return ResponseEntity.ok(
                ApiResponse.success(shiftAssignmentService.getAllAssignments(), "Lấy danh sách phân công ca thành công"));
    }
}
