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
public class AcademicYearRequest {

    @NotBlank(message = "Tên năm học không được để trống")
    @Size(max = 30, message = "Tên năm học không được quá 30 ký tự")
    private String name;

    @NotNull(message = "Ngày bắt đầu năm học không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc năm học không được để trống")
    private LocalDate endDate;

    @NotNull(message = "Trạng thái năm học không được để trống")
    private AcademicStatus status;
}

