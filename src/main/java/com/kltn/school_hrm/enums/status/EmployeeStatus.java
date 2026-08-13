package com.kltn.school_hrm.enums.status;

import lombok.Getter;

/**
 * Trạng thái nhân viên
 */
@Getter
public enum EmployeeStatus {
    WORKING("Đang làm việc"),
    ON_LEAVE("Đang nghỉ phép"),
    RETIRED("Đã nghỉ hưu"),
    RESIGNED("Đã nghỉ việc");

    private final String label;

    EmployeeStatus(String label) {
        this.label = label;
    }
}
