package com.kltn.school_hrm.module.academic.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassSubjectResponse {
    private Long id;
    private Long schoolClassId;
    private String schoolClassName;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private Long semesterId;
    private String semesterName;
    private Integer periodsPerWeek;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

