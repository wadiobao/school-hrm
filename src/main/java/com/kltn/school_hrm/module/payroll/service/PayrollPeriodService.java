package com.kltn.school_hrm.module.payroll.service;

import java.util.List;

import com.kltn.school_hrm.module.payroll.dto.request.PayrollPeriodRequest;
import com.kltn.school_hrm.module.payroll.dto.response.PayrollPeriodResponse;

public interface PayrollPeriodService {

    PayrollPeriodResponse createPeriod(PayrollPeriodRequest request);

    PayrollPeriodResponse getPeriodById(Long id);

    List<PayrollPeriodResponse> getAllPeriods();

    /** Chốt kỳ lương — sau đó không cho phép tính lại */
    PayrollPeriodResponse lockPeriod(Long id);
}
