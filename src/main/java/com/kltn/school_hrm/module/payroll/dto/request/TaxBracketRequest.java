package com.kltn.school_hrm.module.payroll.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO thiết lập từng bậc thuế trong biểu thuế lũy tiến từng phần.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxBracketRequest {

    @NotNull(message = "Thứ tự bậc thuế không được để trống")
    private Integer bracketOrder;

    @NotNull(message = "Ngưỡng bắt đầu không được để trống")
    @DecimalMin(value = "0.0", message = "Ngưỡng bắt đầu phải >= 0")
    private BigDecimal fromAmount;

    /** Ngưỡng kết thúc, null nếu là bậc cao nhất (không giới hạn trần) */
    private BigDecimal toAmount;

    @NotNull(message = "Thuế suất không được để trống")
    @DecimalMin(value = "0.0", message = "Thuế suất phải >= 0")
    private BigDecimal taxRate;

    private String description;
}
