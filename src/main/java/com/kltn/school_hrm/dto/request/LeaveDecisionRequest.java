package com.kltn.school_hrm.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveDecisionRequest {
    @NotNull(message = "Approver ID is required")
    private Long approverId;
    private String comment;
}
