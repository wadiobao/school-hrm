package com.kltn.school_hrm.enums.type;

import lombok.Getter;

/**
 * Loại thành phần lương
 */
@Getter
public enum SalaryComponentType {
    BASE("Lương cơ bản"),
    ALLOWANCE("Phụ cấp"),
    BONUS("Thưởng"),
    DEDUCTION("Khấu trừ");

    private final String label;

    SalaryComponentType(String label) {
        this.label = label;
    }
}
