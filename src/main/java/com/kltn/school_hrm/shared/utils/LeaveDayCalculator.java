package com.kltn.school_hrm.shared.utils;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.kltn.school_hrm.module.core.service.WorkingCalendarService;

import lombok.RequiredArgsConstructor;

/**
 * Utility tính toán số ngày nghỉ phép thực tế dựa trên WorkingCalendarService.
 */
@Component
@RequiredArgsConstructor
public class LeaveDayCalculator {

    private final WorkingCalendarService workingCalendarService;

    public BigDecimal calculate(LocalDate startDate, LocalDate endDate) {
        long workingDays = workingCalendarService.countWorkingDays(startDate, endDate);
        return BigDecimal.valueOf(workingDays);
    }
}

