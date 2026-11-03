package com.kltn.school_hrm.module.academic.dto.request;

import com.kltn.school_hrm.shared.enums.Enums.ClassStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolClassRequest {

    @NotBlank(message = "Tên lớp học không được để trống")
    @Size(max = 50, message = "Tên lớp học không được quá 50 ký tự")
    private String name;

    @NotNull(message = "Khối lớp không được để trống")
    @Min(value = 1, message = "Khối lớp tối thiểu là 1")
    @Max(value = 12, message = "Khối lớp tối đa là 12")
    private Integer gradeLevel;

    @NotNull(message = "ID năm học không được để trống")
    private Long academicYearId;

    private Long homeroomTeacherId;

    @Positive(message = "Sĩ số lớp phải lớn hơn 0")
    private Integer capacity;

    @NotNull(message = "Trạng thái lớp không được để trống")
    private ClassStatus status;
}

