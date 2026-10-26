package com.kltn.school_hrm.module.employee.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.kltn.school_hrm.shared.enums.Enums.Currency;
import com.kltn.school_hrm.shared.enums.Enums.SalaryChangeReason;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO yêu cầu điều chỉnh mức lương của hợp đồng lao động.
 * Lưu vết đầy đủ căn cứ quyết định, ngày hiệu lực và người phê duyệt.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryChangeRequest {

    @NotNull(message = "Mức lương mới không được để trống")
    @DecimalMin(value = "1.0", message = "Mức lương mới phải lớn hơn 0")
    private BigDecimal newSalary;

    private Currency currency;

    @NotNull(message = "Ngày hiệu lực không được để trống")
    private LocalDate effectiveDate;

    @Size(max = 100, message = "Số quyết định không vượt quá 100 ký tự")
    private String decisionNumber;

    @NotNull(message = "Lý do điều chỉnh lương không được để trống")
    private SalaryChangeReason reason;

    @Size(max = 500, message = "Ghi chú không vượt quá 500 ký tự")
    private String note;
}
