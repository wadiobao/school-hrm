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
import com.kltn.school_hrm.module.scheduling.dto.request.TimetableEntryRequest;
import com.kltn.school_hrm.module.scheduling.dto.response.TimetableEntryResponse;
import com.kltn.school_hrm.module.scheduling.service.TimetableEntryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller quản lý các tiết học cụ thể trên thời khóa biểu.
 */
@RestController
@RequestMapping("/api/v1/timetable-entries")
@RequiredArgsConstructor
public class TimetableEntryController {

    private final TimetableEntryService entryService;

    @PostMapping
    public ResponseEntity<ApiResponse<TimetableEntryResponse>> create(@Valid @RequestBody TimetableEntryRequest request) {
        TimetableEntryResponse response = entryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Thêm tiết học vào thời khóa biểu thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TimetableEntryResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody TimetableEntryRequest request) {
        TimetableEntryResponse response = entryService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật tiết học thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TimetableEntryResponse>> getById(@PathVariable Long id) {
        TimetableEntryResponse response = entryService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin tiết học thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TimetableEntryResponse>>> getByTimetableId(
            @RequestParam Long timetableId) {
        List<TimetableEntryResponse> response = entryService.getByTimetableId(timetableId);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách tiết học thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        entryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa tiết học thành công"));
    }
}
