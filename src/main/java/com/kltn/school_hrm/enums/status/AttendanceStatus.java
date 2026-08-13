package com.kltn.school_hrm.enums.status;

import lombok.Getter;

@Getter
public enum AttendanceStatus {

    PRESENT("Có mặt"),
    LATE("Đi muộn"),
    EARLY_LEAVE("Về sớm"),
    ABSENT("Vắng");

    private final String label;

    AttendanceStatus(String label){
        this.label = label;
    }
}
