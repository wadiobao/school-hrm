package com.kltn.school_hrm.module.scheduling.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO phản hồi thời khóa biểu tổng hợp theo góc nhìn Giáo viên.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherTimetableResponse {
    private Long semesterId;
    private String semesterName;
    private String academicYearName;
    private String viewScope; // "ALL" (Toàn trường), "DEPARTMENT" (Cùng bộ môn), "INDIVIDUAL" (Cá nhân)
    private boolean canViewAll;
    private Long departmentId;
    private String departmentName;
    private Long currentEmployeeId;
    private String currentEmployeeName;
    private List<TeacherScheduleRow> rows;
}
