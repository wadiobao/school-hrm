package com.kltn.school_hrm.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.kltn.school_hrm.dto.common.ApiResponse;
import com.kltn.school_hrm.dto.request.PositionCreateRequest;
import com.kltn.school_hrm.dto.response.PositionResponse;
import com.kltn.school_hrm.service.PositionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @PostMapping
    public ResponseEntity<ApiResponse<PositionResponse>> createPosition(@Valid @RequestBody PositionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(positionService.createPosition(request), "Position created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PositionResponse>> updatePosition(@PathVariable Long id, @Valid @RequestBody PositionCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(positionService.updatePosition(id, request), "Position updated successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PositionResponse>> getPositionById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(positionService.getPositionById(id), "Position retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PositionResponse>>> getAllPositions() {
        return ResponseEntity.ok(ApiResponse.success(positionService.getAllPositions(), "Positions retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePosition(@PathVariable Long id) {
        positionService.deletePosition(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Position deleted successfully"));
    }
}
