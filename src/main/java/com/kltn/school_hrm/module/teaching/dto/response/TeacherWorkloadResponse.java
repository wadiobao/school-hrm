package com.kltn.school_hrm.module.teaching.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherWorkloadResponse {
    private Long teacherId;
    private String teacherName;
    private String positionName;
    private String academicYear;
    private Long semesterId;
    private String semesterName;
    private Double totalAssignedPeriods;
    private Integer standardHours;
    private Double effectiveNormHours;
    private Double difference;
    private String workloadStatus; // OVERLOAD, BALANCED, UNDERLOAD
}

