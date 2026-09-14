package com.kltn.school_hrm.dto.response;

import java.time.LocalDateTime;

import com.kltn.school_hrm.enums.Enums.ApprovalActionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalActionResponse {
    private Long id;
    private Long actorId;
    private String actorName;
    private ApprovalActionType action;
    private String comment;
    private LocalDateTime actionAt;
}
