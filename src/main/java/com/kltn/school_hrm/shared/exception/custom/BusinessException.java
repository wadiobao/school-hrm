package com.kltn.school_hrm.shared.exception.custom;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
