package com.kltn.school_hrm.module.payroll.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO yêu cầu tạo mới hoặc cập nhật chính sách thuế, các mức giảm trừ và biểu thuế lũy tiến.
 * Chỉ có Quản trị viên (SUPER_ADMIN) mới có quyền gửi request này.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxPolicyRequest {

    @NotBlank(message = "Tên chính sách không được để trống")
    private String name;

    private String version;

    @NotNull(message = "Ngày bắt đầu hiệu lực không được để trống")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @NotNull(message = "Mức giảm trừ gia cảnh cho bản thân không được để trống")
    @DecimalMin(value = "0.0", message = "Mức giảm trừ bản thân phải >= 0")
    private BigDecimal personalDeduction;

    @NotNull(message = "Mức giảm trừ cho mỗi người phụ thuộc không được để trống")
    @DecimalMin(value = "0.0", message = "Mức giảm trừ người phụ thuộc phải >= 0")
    private BigDecimal dependentDeduction;

    @NotNull(message = "Tỷ lệ bảo hiểm NLĐ không được để trống")
    @DecimalMin(value = "0.0", message = "Tỷ lệ bảo hiểm phải >= 0")
    private BigDecimal insuranceRate;

    private String note;

    @NotEmpty(message = "Biểu thuế lũy tiến phải có ít nhất 1 bậc")
    @Valid
    private List<TaxBracketRequest> brackets;
}
