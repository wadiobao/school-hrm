package com.kltn.school_hrm.module.academic.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassSubjectRequest {

    @NotNull(message = "ID lớp học không được để trống")
    private Long schoolClassId;

    @NotNull(message = "ID môn học không được để trống")
    private Long subjectId;

    @NotNull(message = "ID học kỳ không được để trống")
    private Long semesterId;

    @NotNull(message = "Số tiết/tuần không được để trống")
    @Min(value = 1, message = "Số tiết/tuần tối thiểu phải là 1")
    private Integer periodsPerWeek;
}

