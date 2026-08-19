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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kltn.school_hrm.dto.common.ApiResponse;
import com.kltn.school_hrm.dto.request.LeaveCreateRequest;
import com.kltn.school_hrm.dto.response.LeaveResponse;
import com.kltn.school_hrm.service.LeaveService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping
    public ResponseEntity<ApiResponse<LeaveResponse>> createLeaveRequest(@Valid @RequestBody LeaveCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(leaveService.createLeaveRequest(request), "Leave request created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveResponse>> updateLeaveRequest(@PathVariable Long id, @Valid @RequestBody LeaveCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.updateLeaveRequest(id, request), "Leave request updated successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveResponse>> getLeaveRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getLeaveRequestById(id), "Leave request retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LeaveResponse>>> getAllLeaveRequests() {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getAllLeaveRequests(), "Leave requests retrieved successfully"));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<LeaveResponse>>> getLeaveRequestsByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getLeaveRequestsByEmployeeId(employeeId), "Leave requests retrieved successfully"));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<LeaveResponse>> approveLeaveRequest(@PathVariable Long id, @RequestParam Long approverId) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.approveLeaveRequest(id, approverId), "Leave request approved"));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<LeaveResponse>> rejectLeaveRequest(@PathVariable Long id, @RequestParam Long approverId) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.rejectLeaveRequest(id, approverId), "Leave request rejected"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLeaveRequest(@PathVariable Long id) {
        leaveService.deleteLeaveRequest(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Leave request deleted successfully"));
    }
}
