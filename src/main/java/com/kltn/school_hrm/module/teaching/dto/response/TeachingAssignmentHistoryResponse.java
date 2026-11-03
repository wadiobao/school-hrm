package com.kltn.school_hrm.module.teaching.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeachingAssignmentHistoryResponse {
    private Long id;
    private Long teachingAssignmentId;
    private Long oldTeacherId;
    private String oldTeacherName;
    private Long newTeacherId;
    private String newTeacherName;
    private String changedBy;
    private LocalDateTime changedAt;
    private String reason;
}

