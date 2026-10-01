package com.kltn.school_hrm.module.teaching.dto.response;

import com.kltn.school_hrm.shared.enums.Enums.Curriculum;

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
    private Long teacherId;
    private Curriculum curriculum;
    private String subjectName;
    private String gradeLevel;
    private Double weeklyContactHours;
}
