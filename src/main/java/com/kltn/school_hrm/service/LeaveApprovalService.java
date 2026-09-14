package com.kltn.school_hrm.service;

import com.kltn.school_hrm.entity.attendance.LeaveApproval;
import com.kltn.school_hrm.entity.attendance.LeaveRequest;
import com.kltn.school_hrm.entity.employee.Employee;

/**
 * @deprecated Thay thế bằng {@link com.kltn.school_hrm.service.approval.ApprovalEngineService}.
 */
@Deprecated
public interface LeaveApprovalService {

    void createApprovalSteps(LeaveRequest request);

    void approveCurrentStep(LeaveRequest request, Long approverId, String comment);

    void rejectCurrentStep(LeaveRequest request, Long approverId, String comment);

    boolean isFullyApproved(LeaveRequest request);

    LeaveApproval getCurrentApproval(LeaveRequest request);
}
