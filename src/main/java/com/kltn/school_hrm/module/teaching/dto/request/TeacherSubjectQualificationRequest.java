package com.kltn.school_hrm.module.teaching.dto.request;

import com.kltn.school_hrm.shared.enums.Enums.SubjectStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherSubjectQualificationRequest {

    @NotNull(message = "ID giáo viên không được để trống")
    private Long teacherId;

    @NotNull(message = "ID môn học không được để trống")
    private Long subjectId;

    private String qualificationLevel;

    @NotNull(message = "Trạng thái không được để trống")
    private SubjectStatus status;
}

