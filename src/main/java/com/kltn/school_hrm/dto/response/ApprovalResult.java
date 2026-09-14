package com.kltn.school_hrm.dto.response;

import java.time.LocalDateTime;

import com.kltn.school_hrm.enums.Enums.ApprovalStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalResult {

    private Long approvalRequestId;
    private String businessType;
    private Long businessId;
    private Integer currentLevel;
    private ApprovalStatus status;
    private boolean fullyApproved;
    private boolean rejected;
    private Long currentApproverId;
    private String currentApproverName;
    private String comment;
    private LocalDateTime actionAt;
}
