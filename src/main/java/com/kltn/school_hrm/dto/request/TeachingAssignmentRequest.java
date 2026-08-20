package com.kltn.school_hrm.dto.request;

import com.kltn.school_hrm.enums.Enums.Curriculum;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeachingAssignmentRequest {
    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    private Curriculum curriculum;

    private String subjectName;

    private String gradeLevel;

    @Positive
    private Double weeklyContactHours;
}
