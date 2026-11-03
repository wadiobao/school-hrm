package com.kltn.school_hrm.module.scheduling.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableEntryResponse {
    private Long id;
    private Long timetableId;
    private Long teachingAssignmentId;
    private String subjectName;
    private String subjectCode;
    private Long teacherId;
    private String teacherName;
    private Long substituteTeacherId;
    private String substituteTeacherName;
    private Integer dayOfWeek;
    private String dayOfWeekLabel;
    private Long timeSlotId;
    private Integer periodNumber;
    private String timeSlotRange;
    private Long classroomId;
    private String classroomCode;
    private String classroomName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
