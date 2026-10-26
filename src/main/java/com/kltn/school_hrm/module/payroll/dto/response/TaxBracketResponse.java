package com.kltn.school_hrm.module.payroll.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO phản hồi thông tin từng bậc thuế trong biểu thuế lũy tiến.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxBracketResponse {

    private Long id;
    private Integer bracketOrder;
    private BigDecimal fromAmount;
    private BigDecimal toAmount;
    private BigDecimal taxRate;
    private String description;
}
