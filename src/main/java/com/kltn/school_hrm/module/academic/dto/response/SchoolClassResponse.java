package com.kltn.school_hrm.module.academic.dto.response;

import java.time.LocalDateTime;

import com.kltn.school_hrm.shared.enums.Enums.ClassStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolClassResponse {
    private Long id;
    private String name;
    private Integer gradeLevel;
    private Long academicYearId;
    private String academicYearName;
    private Long homeroomTeacherId;
    private String homeroomTeacherName;
    private Integer capacity;
    private ClassStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

