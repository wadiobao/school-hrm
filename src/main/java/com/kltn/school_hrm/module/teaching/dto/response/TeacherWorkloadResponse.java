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
    private String departmentName;
    private String academicYear;
    private Long semesterId;
    private String semesterName;
    private Double totalAssignedPeriods;
    private Integer standardHours;
    private Double reductionHours;
    private String reductionDetails;
    private Double effectiveNormHours;
    private Double difference;
    private Double yearlyDifference35Weeks;
    private String workloadStatus; // OVERLOAD, BALANCED, UNDERLOAD
    private String complianceNote; // Hợp lệ, Thiếu định mức, Vượt trần 200 tiết/năm (TT 21/2025)
}
