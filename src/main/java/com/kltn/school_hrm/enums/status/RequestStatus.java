package com.kltn.school_hrm.enums.status;

import lombok.Getter;

@Getter
public enum RequestStatus {
    PENDING("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    REJECTED("Từ chối");

    private final String label;

    RequestStatus(String label){
        this.label = label;
    }
}
