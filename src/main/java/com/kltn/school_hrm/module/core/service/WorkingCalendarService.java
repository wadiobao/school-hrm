package com.kltn.school_hrm.module.core.service;

import java.time.LocalDate;

/**
 * Service tính toán ngày làm việc chuẩn (Working Calendar).
 */
public interface WorkingCalendarService {

    /**
     * Đếm số ngày làm việc tiêu chuẩn giữa startDate và endDate (bao gồm cả 2 đầu ngày).
     * Loại trừ các ngày cuối tuần (T7, CN) và các ngày nghỉ lễ.
     *
     * @param startDate ngày bắt đầu
     * @param endDate ngày kết thúc
     * @return số ngày làm việc chuẩn
     * @throws IllegalArgumentException nếu startDate hoặc endDate null, hoặc startDate sau endDate
     */
    long countWorkingDays(LocalDate startDate, LocalDate endDate);

    /**
     * Kiểm tra một ngày cụ thể có phải là ngày làm việc chuẩn hay không (không phải cuối tuần & không phải ngày lễ).
     *
     * @param date ngày cần kiểm tra
     * @return true nếu là ngày làm việc
     */
    boolean isWorkingDay(LocalDate date);
}
