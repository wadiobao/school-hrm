package com.kltn.school_hrm.module.leave.service;

import java.util.List;

import com.kltn.school_hrm.module.leave.dto.request.LeaveCreateRequest;
import com.kltn.school_hrm.module.leave.dto.request.LeaveDecisionRequest;
import com.kltn.school_hrm.module.leave.dto.response.LeaveResponse;

public interface LeaveRequestService {
    LeaveResponse createLeaveRequest(LeaveCreateRequest request);

    LeaveResponse updateLeaveRequest(Long id, LeaveCreateRequest request);

    LeaveResponse getLeaveRequestById(Long id);

    List<LeaveResponse> getAllLeaveRequests();

    List<LeaveResponse> getLeaveRequestsByEmployeeId(Long employeeId);

    LeaveResponse approveLeaveRequest(Long id, LeaveDecisionRequest request);

    LeaveResponse rejectLeaveRequest(Long id, LeaveDecisionRequest request);

    List<LeaveResponse> getOverdueLeaveRequests();

    List<LeaveResponse> processOverdueLeaveRequests();
}

