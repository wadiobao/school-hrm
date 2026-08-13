package com.kltn.school_hrm.enums.type;

import lombok.Getter;

/**
 * Loại nghỉ phép
 */
@Getter
public enum LeaveType {
    ANNUAL("Nghỉ phép năm"),
    SICK("Nghỉ ốm"),
    MATERNITY("Nghỉ thai sản"),
    UNPAID("Nghỉ không lương");

    private final String label;

    LeaveType(String label) {
        this.label = label;
    }
}
