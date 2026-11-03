package com.kltn.school_hrm.module.scheduling.controller;

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
import com.kltn.school_hrm.module.scheduling.dto.request.TimeSlotRequest;
import com.kltn.school_hrm.module.scheduling.dto.response.TimeSlotResponse;
import com.kltn.school_hrm.module.scheduling.service.TimeSlotService;
import com.kltn.school_hrm.shared.enums.Enums.TimeSlotSession;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller quản lý khung giờ / tiết học trong ngày.
 */
@RestController
@RequestMapping("/api/v1/time-slots")
@RequiredArgsConstructor
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    @PostMapping
    public ResponseEntity<ApiResponse<TimeSlotResponse>> create(@Valid @RequestBody TimeSlotRequest request) {
        TimeSlotResponse response = timeSlotService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Thêm tiết học thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TimeSlotResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody TimeSlotRequest request) {
        TimeSlotResponse response = timeSlotService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật tiết học thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TimeSlotResponse>> getById(@PathVariable Long id) {
        TimeSlotResponse response = timeSlotService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin tiết học thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TimeSlotResponse>>> getAll(
            @RequestParam(required = false) TimeSlotSession session) {
        List<TimeSlotResponse> response;
        if (session != null) {
            response = timeSlotService.getBySession(session);
        } else {
            response = timeSlotService.getAll();
        }
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách tiết học thành công"));
    }

    @PostMapping("/init-defaults")
    public ResponseEntity<ApiResponse<Void>> initDefaultSlots() {
        timeSlotService.initDefaultSlotsIfEmpty();
        return ResponseEntity.ok(ApiResponse.success(null, "Khởi tạo tiết học mẫu thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        timeSlotService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa tiết học thành công"));
    }
}
