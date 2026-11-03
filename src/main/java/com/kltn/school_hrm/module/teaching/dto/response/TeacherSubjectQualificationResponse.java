package com.kltn.school_hrm.module.teaching.dto.response;

import java.time.LocalDateTime;

import com.kltn.school_hrm.shared.enums.Enums.SubjectStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherSubjectQualificationResponse {
    private Long id;
    private Long teacherId;
    private String teacherName;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private String qualificationLevel;
    private SubjectStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

