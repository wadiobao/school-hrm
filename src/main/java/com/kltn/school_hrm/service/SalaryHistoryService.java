package com.kltn.school_hrm.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.kltn.school_hrm.entity.history.SalaryHistory;
import com.kltn.school_hrm.enums.Enums.Currency;
import com.kltn.school_hrm.enums.Enums.SalaryChangeReason;

public interface SalaryHistoryService {
    SalaryHistory changeSalary(
            Long contractId,
            BigDecimal newSalary,
            Currency currency,
            LocalDate effectiveDate,
            String decisionNumber,
            SalaryChangeReason reason,
            Long approvedBy);

    List<SalaryHistory> getHistoryByContract(Long contractId);

    SalaryHistory getLatest(Long contractId);
}
