package com.kltn.school_hrm.module.academic.controller;

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
import com.kltn.school_hrm.module.academic.dto.request.ClassroomRequest;
import com.kltn.school_hrm.module.academic.dto.response.ClassroomResponse;
import com.kltn.school_hrm.module.academic.service.ClassroomService;
import com.kltn.school_hrm.shared.enums.Enums.ClassroomStatus;
import com.kltn.school_hrm.shared.enums.Enums.ClassroomType;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller quản lý danh mục phòng học, cơ sở vật chất.
 */
@RestController
@RequestMapping("/api/v1/classrooms")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClassroomResponse>> create(@Valid @RequestBody ClassroomRequest request) {
        ClassroomResponse response = classroomService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Thêm phòng học thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassroomResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ClassroomRequest request) {
        ClassroomResponse response = classroomService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật phòng học thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassroomResponse>> getById(@PathVariable Long id) {
        ClassroomResponse response = classroomService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin phòng học thành công"));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<ClassroomResponse>> getByCode(@PathVariable String code) {
        ClassroomResponse response = classroomService.getByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin phòng học thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClassroomResponse>>> getAll(
            @RequestParam(required = false) ClassroomStatus status,
            @RequestParam(required = false) ClassroomType roomType) {
        List<ClassroomResponse> response;
        if (status != null) {
            response = classroomService.getByStatus(status);
        } else if (roomType != null) {
            response = classroomService.getByRoomType(roomType);
        } else {
            response = classroomService.getAll();
        }
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách phòng học thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        classroomService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa phòng học thành công"));
    }
}

