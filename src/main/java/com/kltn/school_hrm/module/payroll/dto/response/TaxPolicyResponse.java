package com.kltn.school_hrm.module.payroll.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO phản hồi thông tin đầy đủ về chính sách thuế, các mức giảm trừ và danh sách các bậc thuế.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxPolicyResponse {

    private Long id;
    private String code;
    private String name;
    private String version;

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    private BigDecimal personalDeduction;
    private BigDecimal dependentDeduction;
    private BigDecimal insuranceRate;

    private Boolean isActive;
    private String note;

    private List<TaxBracketResponse> brackets;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
