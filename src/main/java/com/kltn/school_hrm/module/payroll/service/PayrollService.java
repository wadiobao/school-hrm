package com.kltn.school_hrm.module.payroll.service;

import java.util.List;

import com.kltn.school_hrm.module.payroll.dto.request.PayrollCreateRequest;
import com.kltn.school_hrm.module.payroll.dto.request.PayrollPaymentRequest;
import com.kltn.school_hrm.module.payroll.dto.request.PayrollTransitionRequest;
import com.kltn.school_hrm.module.payroll.dto.response.PayrollHistoryResponse;
import com.kltn.school_hrm.module.payroll.dto.response.PayrollPaymentResponse;
import com.kltn.school_hrm.module.payroll.dto.response.PayrollResponse;
import com.kltn.school_hrm.shared.enums.Enums.PayrollStatus;

public interface PayrollService {

    PayrollResponse createPayroll(PayrollCreateRequest request);

    PayrollResponse updatePayroll(Long id, PayrollCreateRequest request);

    PayrollResponse getPayrollById(Long id);

    List<PayrollResponse> getAllPayrolls();

    List<PayrollResponse> getPayrollsByEmployeeId(Long employeeId);

    List<PayrollResponse> getPayrollsByPeriodId(Long periodId);

    List<PayrollResponse> getPayrollsByMonthAndYear(Integer month, Integer year);

    /**
     * Cập nhật trạng thái cơ bản (backward compatibility).
     */
    PayrollResponse updatePayrollStatus(Long id, PayrollStatus status);

    /**
     * Điều phối chuyển đổi vòng đời bảng lương thông qua State Machine với Pessimistic Lock,
     * tự động đóng băng snapshot khi LOCKED và kiểm tra điều kiện tài chính trước khi PAID.
     */
    PayrollResponse transitionStatus(Long id, PayrollTransitionRequest request, Long currentUserId, String currentUserName);

    /**
     * Ghi nhận đợt thanh toán từ bộ phận Tài chính, kiểm tra đối soát tổng chi trả
     * để tự động hoặc cho phép hoàn tất chuyển trạng thái bảng lương sang PAID.
     */
    PayrollPaymentResponse recordPayment(Long id, PayrollPaymentRequest request, Long currentUserId, String currentUserName);

    /**
     * Lấy toàn bộ lịch sử chuyển trạng thái (audit trail) của một bảng lương cụ thể.
     */
    List<PayrollHistoryResponse> getHistoriesByPayrollId(Long id);

    /**
     * Lấy danh sách các đợt thanh toán đã thực hiện cho bảng lương này.
     */
    List<PayrollPaymentResponse> getPaymentsByPayrollId(Long id);

    void deletePayroll(Long id);
}
