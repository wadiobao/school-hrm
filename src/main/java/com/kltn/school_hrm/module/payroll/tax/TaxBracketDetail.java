package com.kltn.school_hrm.module.payroll.tax;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Chi tiết tính thuế trên từng bậc trong biểu thuế lũy tiến từng phần.
 * Phục vụ mục đích audit, giải trình phiếu lương minh bạch cho nhân viên.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxBracketDetail {

    private int bracketOrder;
    private BigDecimal fromAmount;
    private BigDecimal toAmount;
    private BigDecimal taxRate;
    private BigDecimal taxableAmountInBracket;
    private BigDecimal taxInBracket;
}
