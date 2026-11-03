package com.kltn.school_hrm.module.teaching.dto.response;

import java.time.LocalDateTime;

import com.kltn.school_hrm.shared.enums.Enums.Curriculum;
import com.kltn.school_hrm.shared.enums.Enums.TeachingAssignmentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeachingAssignmentResponse {
    private Long id;
    private Long schoolClassId;
    private String schoolClassName;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private Long teacherId;
    private String teacherName;
    private Curriculum curriculum;
    private String gradeLevel;
    private Double weeklyContactHours;
    private TeachingAssignmentStatus status;
    private LocalDateTime assignedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
