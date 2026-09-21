package com.kltn.school_hrm.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kltn.school_hrm.dto.common.ApiResponse;
import com.kltn.school_hrm.dto.request.ShiftRequest;
import com.kltn.school_hrm.dto.response.ShiftResponse;
import com.kltn.school_hrm.service.ShiftService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    /** POST /api/v1/shifts — Tạo ca làm việc mới */
    @PostMapping
    public ResponseEntity<ApiResponse<ShiftResponse>> createShift(
            @Valid @RequestBody ShiftRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(shiftService.createShift(request), "Tạo ca làm việc thành công"));
    }

    /** PUT /api/v1/shifts/{id} — Cập nhật ca làm việc */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ShiftResponse>> updateShift(
            @PathVariable Long id,
            @Valid @RequestBody ShiftRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(shiftService.updateShift(id, request), "Cập nhật ca làm việc thành công"));
    }

    /** GET /api/v1/shifts/{id} — Lấy một ca theo id */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShiftResponse>> getShiftById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(shiftService.getShiftById(id), "Lấy thông tin ca làm việc thành công"));
    }

    /** GET /api/v1/shifts — Lấy tất cả ca */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ShiftResponse>>> getAllShifts() {
        return ResponseEntity.ok(
                ApiResponse.success(shiftService.getAllShifts(), "Lấy danh sách ca làm việc thành công"));
    }

    /** GET /api/v1/shifts/active — Lấy các ca đang hoạt động */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ShiftResponse>>> getActiveShifts() {
        return ResponseEntity.ok(
                ApiResponse.success(shiftService.getActiveShifts(), "Lấy danh sách ca đang hoạt động thành công"));
    }

    /** DELETE /api/v1/shifts/{id} — Xóa ca làm việc */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShift(@PathVariable Long id) {
        shiftService.deleteShift(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa ca làm việc thành công"));
    }

    /** PATCH /api/v1/shifts/{id}/toggle — Bật/tắt trạng thái hoạt động */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<ShiftResponse>> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(shiftService.toggleActive(id), "Cập nhật trạng thái ca làm việc thành công"));
    }
}
