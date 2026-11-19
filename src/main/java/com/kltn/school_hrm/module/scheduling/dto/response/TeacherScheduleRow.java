package com.kltn.school_hrm.module.scheduling.dto.response;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO đại diện cho một hàng trong bảng thời khóa biểu giáo viên (1 giáo viên với lịch các thứ trong tuần).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherScheduleRow {
    private Long teacherId;
    private String teacherCode;
    private String teacherName;
    private String departmentName;
    private String positionName;
    private int totalPeriods;
    // Map theo Thứ trong tuần: 1 = Thứ 2, 2 = Thứ 3, ..., 7 = Chủ nhật
    private Map<Integer, List<TeacherPeriodItem>> scheduleByDay;
}
