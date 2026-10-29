package com.kltn.school_hrm.module.formula.dto.request;

import java.time.LocalDate;

import com.kltn.school_hrm.shared.enums.Enums.FormulaStatus;

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
public class FormulaDefinitionRequest {

    @NotBlank(message = "Mã công thức không được để trống")
    @Size(max = 50, message = "Mã công thức tối đa 50 ký tự")
    private String code;

    @NotBlank(message = "Tên công thức không được để trống")
    @Size(max = 150, message = "Tên công thức tối đa 150 ký tự")
    private String name;

    @NotBlank(message = "Biểu thức không được để trống")
    @Size(max = 500, message = "Biểu thức tối đa 500 ký tự")
    private String expression;

    @Size(max = 20, message = "Phiên bản tối đa 20 ký tự")
    private String version;

    @NotNull(message = "Ngày bắt đầu áp dụng không được để trống")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    private FormulaStatus status;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String note;
}
