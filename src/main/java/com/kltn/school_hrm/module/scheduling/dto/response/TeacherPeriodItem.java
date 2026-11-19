package com.kltn.school_hrm.module.scheduling.dto.response;

import com.kltn.school_hrm.shared.enums.Enums.TimeSlotSession;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO đại diện cho một tiết dạy cụ thể của giáo viên trong ngày.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherPeriodItem {
    private Long entryId;
    private Integer periodNumber;
    private String timeSlotRange;
    private TimeSlotSession session;
    private Long schoolClassId;
    private String schoolClassName;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private Long classroomId;
    private String classroomCode;
    private String classroomName;
    private boolean isSubstitute;
    private String substituteNote;
}
