package com.kltn.school_hrm.module.academic.dto.request;

import java.time.LocalDate;

import com.kltn.school_hrm.shared.enums.Enums.AcademicStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SemesterRequest {

    @NotNull(message = "ID năm học không được để trống")
    private Long academicYearId;

    @NotBlank(message = "Tên học kỳ không được để trống")
    @Size(max = 50, message = "Tên học kỳ không được quá 50 ký tự")
    private String name;

    @NotNull(message = "Ngày bắt đầu học kỳ không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc học kỳ không được để trống")
    private LocalDate endDate;

    @NotNull(message = "Trạng thái học kỳ không được để trống")
    private AcademicStatus status;
}

