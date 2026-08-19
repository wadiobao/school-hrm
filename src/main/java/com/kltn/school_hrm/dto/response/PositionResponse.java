package com.kltn.school_hrm.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionResponse {
    private Long id;
    private String code;
    private String name;
    private BigDecimal positionAllowanceRate;
}
