package com.kltn.school_hrm.module.leave.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.kltn.school_hrm.shared.enums.Enums.RequestStatus;
import com.kltn.school_hrm.shared.enums.Enums.LeaveType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.kltn.school_hrm.module.approval.dto.response.ApprovalActionResponse;
import com.kltn.school_hrm.module.approval.dto.response.ApprovalStepResponse;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveResponse {
    private Long id;
    private Long employeeId;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private java.math.BigDecimal totalDays;
    private String reason;
    private Long substituteTeacherId;
    private List<Long> approverId;
    private RequestStatus status;
    private List<ApprovalStepResponse> approvalSteps;
    private List<ApprovalActionResponse> approvalActions;
}


