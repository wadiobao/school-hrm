package com.kltn.school_hrm.enums.type;

import lombok.Getter;

@Getter
public enum ContractType {
    DEFINITE("Hợp đồng xác định thời hạn"),
    INDEFINITE("Hợp đồng không xác định thời hạn"),
    VISITING("Hợp đồng thỉnh giảng");

    private final String label;

    ContractType(String label) {
        this.label = label;
    }
}
