package com.kltn.school_hrm.service;

import java.util.List;

import com.kltn.school_hrm.dto.request.LeaveCreateRequest;
import com.kltn.school_hrm.dto.response.LeaveResponse;

public interface LeaveService {
    LeaveResponse createLeaveRequest(LeaveCreateRequest request);
    LeaveResponse updateLeaveRequest(Long id, LeaveCreateRequest request);
    LeaveResponse getLeaveRequestById(Long id);
    List<LeaveResponse> getAllLeaveRequests();
    List<LeaveResponse> getLeaveRequestsByEmployeeId(Long employeeId);
    LeaveResponse approveLeaveRequest(Long id, Long approverId);
    LeaveResponse rejectLeaveRequest(Long id, Long approverId);
    void deleteLeaveRequest(Long id);
}
