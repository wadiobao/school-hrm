package com.kltn.school_hrm.module.scheduling.dto.response;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kết quả trả về sau khi chạy thuật toán tự động phân bổ giáo viên và xếp thời khóa biểu học kỳ.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoScheduleResultResponse {

    private Long semesterId;
    private String semesterName;
    private String academicYearName;

    private int totalClassesScheduled;
    private int totalAssignmentsAllocated;
    private int totalPeriodsScheduled;
    private int unplacedPeriods;
    private long executionTimeMs;

    /**
     * Bảng quy đổi số tiết/tuần cho từng môn học được tính từ tỉ lệ cấu hình.
     */
    private Map<String, Integer> allocatedPeriodsPerSubject;

    /**
     * Chi tiết kết quả xếp lịch cho từng lớp học.
     */
    private List<ClassScheduleSummary> classResults;

    /**
     * Tổng hợp tải giảng dạy (số tiết/tuần) của các giáo viên sau khi phân bổ tự động
     * theo Thông tư 05/2025/TT-BGDĐT & Thông tư 21/2025/TT-BGDĐT.
     */
    private List<TeacherLoadSummary> teacherLoads;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClassScheduleSummary {
        private Long schoolClassId;
        private String schoolClassName;
        private Integer gradeLevel;
        private Long timetableId;
        private Integer version;
        private String status;
        private int requiredPeriods;
        private int scheduledPeriods;
        private boolean valid;
        private List<String> assignedTeachers;
        private List<String> notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeacherLoadSummary {
        private Long teacherId;
        private String teacherCode;
        private String teacherName;
        private String departmentName;
        private int standardNormHours;
        private int reductionHours;
        private String reductionDetails;
        private int effectiveNormHours;
        private int assignedWeeklyPeriods;
        private int weeklyDifference;
        private int yearlyDifference35Weeks;
        private int classesCount;
        private double loadPercentage;
        private String complianceStatus;
    }
}
