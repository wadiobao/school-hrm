package com.kltn.school_hrm.service.implement;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.entity.attendance.LeaveBalance;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.exception.custom.BusinessException;
import com.kltn.school_hrm.repository.LeaveBalanceRepository;
import com.kltn.school_hrm.service.LeaveBalanceService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;

    @Override
    public void reserve(Employee employee, int year, int days) {
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndYear(employee.getId(), year)
                .orElseThrow(() -> new BusinessException(
                        "Không tìm thấy quỹ phép"));

        BigDecimal available = balance.getTotalDays()
                .subtract(balance.getUsedDays())
                .subtract(balance.getPendingDays());

        if (available.compareTo(BigDecimal.valueOf(days)) < 0) {
            throw new BusinessException(
                    "Không đủ ngày phép");

        }

        balance.setPendingDays(balance.getPendingDays().add(BigDecimal.valueOf(days)));
    }

    @Override
    public void consume(Employee employee, int year, int days) {
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndYear(employee.getId(), year)
                .orElseThrow(() -> new BusinessException(
                        "Không tìm thấy quỹ phép"));

        balance.setUsedDays(balance.getUsedDays().add(BigDecimal.valueOf(days)));
        balance.setPendingDays(balance.getPendingDays().subtract(BigDecimal.valueOf(days)));
    }

}
