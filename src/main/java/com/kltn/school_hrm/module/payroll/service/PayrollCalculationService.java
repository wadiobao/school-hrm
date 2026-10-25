package com.kltn.school_hrm.module.payroll.service;

import java.util.List;

import com.kltn.school_hrm.module.payroll.dto.response.PayrollResponse;

/**
 * Tính lương tự động cho toàn bộ nhân viên trong một kỳ lương.
 */
public interface PayrollCalculationService {

    /**
     * Tính lương tự động cho tất cả nhân viên WORKING/PROBATION trong kỳ.
     *
     * @param periodId ID của kỳ lương cần tính
     * @return danh sách bảng lương đã được lưu (DRAFT)
     */
    List<PayrollResponse> calculateForPeriod(Long periodId);

    /**
     * Tính (hoặc tính lại) lương cho một nhân viên cụ thể trong kỳ.
     * Nếu đã tồn tại bản DRAFT thì xóa và tính lại; không cho phép tính lại khi PAID.
     *
     * @param periodId   ID kỳ lương
     * @param employeeId ID nhân viên
     */
    PayrollResponse calculateForEmployee(Long periodId, Long employeeId);
}
