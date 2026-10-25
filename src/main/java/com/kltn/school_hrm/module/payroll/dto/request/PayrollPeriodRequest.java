package com.kltn.school_hrm.module.payroll.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
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
public class PayrollPeriodRequest {

    @NotNull(message = "Tháng không được để trống")
    @Min(1) @Max(12)
    private Integer month;

    @NotNull(message = "Năm không được để trống")
    @Min(2000)
    private Integer year;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    @NotNull(message = "Số ngày công chuẩn không được để trống")
    @Min(1)
    private Integer standardWorkDays;

    private String note;
}
