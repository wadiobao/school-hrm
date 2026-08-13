package com.kltn.school_hrm.enums.type;

import lombok.Getter;

@Getter
public enum EmployeeType {
    FULL_TIME("Toàn thời gian"),
    PART_TIME("Bán thời gian"),
    STAFF("Hành chính");

    private final String label;

    EmployeeType(String label) {
        this.label = label;
    }
}
