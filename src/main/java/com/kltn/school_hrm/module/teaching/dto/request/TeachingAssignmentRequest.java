package com.kltn.school_hrm.module.teaching.dto.request;

import com.kltn.school_hrm.shared.enums.Enums.Curriculum;
import com.kltn.school_hrm.shared.enums.Enums.TeachingAssignmentStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeachingAssignmentRequest {

    private Long schoolClassId;

    private Long subjectId;

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    private Curriculum curriculum;

    private String subjectName;

    private String gradeLevel;

    private Double weeklyContactHours;

    private TeachingAssignmentStatus status;

    private String transferReason; // Lý do chuyển giao nếu thay đổi giáo viên
}
