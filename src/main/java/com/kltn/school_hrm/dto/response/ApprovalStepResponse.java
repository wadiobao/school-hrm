package com.kltn.school_hrm.dto.response;

import java.time.LocalDateTime;

import com.kltn.school_hrm.enums.Enums.ApprovalStatus;
import com.kltn.school_hrm.enums.Enums.ApproverType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalStepResponse {
    private Long id;
    private Integer levelOrder;
    private String levelName;
    private ApproverType approverType;
    private Long assignedApproverId;
    private String assignedApproverName;
    private ApprovalStatus status;
    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;
}
