package com.kltn.school_hrm.service;

import java.util.List;

import com.kltn.school_hrm.dto.request.PayrollCreateRequest;
import com.kltn.school_hrm.dto.response.PayrollResponse;
import com.kltn.school_hrm.enums.Enums.PayrollStatus;

public interface PayrollService {
    PayrollResponse createPayroll(PayrollCreateRequest request);
    PayrollResponse updatePayroll(Long id, PayrollCreateRequest request);
    PayrollResponse getPayrollById(Long id);
    List<PayrollResponse> getAllPayrolls();
    List<PayrollResponse> getPayrollsByEmployeeId(Long employeeId);
    List<PayrollResponse> getPayrollsByMonthAndYear(Integer month, Integer year);
    PayrollResponse updatePayrollStatus(Long id, PayrollStatus status);
    void deletePayroll(Long id);
}
