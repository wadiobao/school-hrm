package com.kltn.school_hrm.module.leave.service.implement;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.leave.entity.LeaveBalance;
import com.kltn.school_hrm.module.leave.entity.LeaveRequest;
import com.kltn.school_hrm.shared.enums.Enums.EmployeeStatus;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.module.leave.repository.LeaveBalanceRepository;
import com.kltn.school_hrm.module.leave.service.LeaveBalanceService;
import com.kltn.school_hrm.module.leave.service.LeaveBalanceTransactionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveBalanceTransactionService leaveBalanceTransactionService;
    private final EmployeeRepository employeeRepository;

    @Override
    public void reserve(Employee employee, int year, BigDecimal days) {
        if (days == null || days.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Số ngày nghỉ phải lớn hơn 0");
        }

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndYear(employee.getId(), year)
                .orElseThrow(() -> new BusinessException("Không tìm thấy quỹ phép"));

        BigDecimal available = balance.getRemainingDays();

        if (available.compareTo(days) < 0) {
            throw new BusinessException("Không đủ ngày phép");
        }

        balance.setPendingDays(balance.getPendingDays().add(days));
    }

    @Override
    public void consume(Employee employee, int year, BigDecimal days, LeaveRequest leaveRequest) {
        if (days == null || days.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Số ngày nghỉ phải lớn hơn 0");
        }

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndYear(employee.getId(), year)
                .orElseThrow(() -> new BusinessException("Không tìm thấy quỹ phép"));

        balance.setUsedDays(balance.getUsedDays().add(days));
        balance.setPendingDays(balance.getPendingDays().subtract(days));

        leaveBalanceTransactionService.createApprovedLeaveBalanceTransaction(balance, days, leaveRequest);
    }

    @Override
    public void release(Employee employee, int year, BigDecimal days) {
        if (days == null || days.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Số ngày nghỉ phải lớn hơn 0");
        }

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndYear(employee.getId(), year)
                .orElseThrow(() -> new BusinessException("Không tìm thấy quỹ phép"));

        balance.setPendingDays(balance.getPendingDays().subtract(days));
    }

    public void accrue(Employee employee, int year, BigDecimal days, String reason) {
        if (days == null || days.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Số ngày phép tích lũy phải lớn hơn 0");
        }

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndYear(employee.getId(), year)
                .orElseGet(() -> leaveBalanceRepository.save(LeaveBalance.builder()
                        .employee(employee)
                        .year(year)
                        .totalDays(BigDecimal.ZERO)
                        .usedDays(BigDecimal.ZERO)
                        .pendingDays(BigDecimal.ZERO)
                        .build()));

        balance.setTotalDays(days);
        balance.setPendingDays(BigDecimal.ZERO);
        balance.setUsedDays(BigDecimal.ZERO);

        leaveBalanceTransactionService.createAccrualTransaction(balance, days, reason);
    }

    @Override
    public void adjust(Employee employee, int year, BigDecimal days, String reason) {
        if (days == null || days.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException("Số ngày phép điều chỉnh phải khác 0");
        }

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndYear(employee.getId(), year)
                .orElseThrow(() -> new BusinessException("Không tìm thấy quỹ phép để điều chỉnh"));

        BigDecimal newTotalDays = balance.getTotalDays().add(days);
        if (newTotalDays.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Tổng số ngày phép sau điều chỉnh không được nhỏ hơn 0");
        }

        BigDecimal newRemainingDays = balance.getRemainingDays().add(days);
        if (newRemainingDays.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Số ngày phép khả dụng sau điều chỉnh không được âm");
        }

        balance.setTotalDays(newTotalDays);

        leaveBalanceTransactionService.createAdjustmentTransaction(balance, days, reason);
    }

    @Override
    public void expireYear(int year) {
        List<LeaveBalance> balances = leaveBalanceRepository.findByYear(year);
        for (LeaveBalance balance : balances) {
            BigDecimal remainingDays = balance.getRemainingDays();
            // Nếu còn ngày phép khả dụng lớn hơn 0
            if (remainingDays != null && remainingDays.compareTo(BigDecimal.ZERO) > 0) {
                balance.expire(remainingDays);
                leaveBalanceTransactionService.createExpirationTransaction(
                        balance,
                        remainingDays,
                        "Hết hạn ngày phép năm " + year);
            }
        }
    }

    @Override
    public void accrueAnnualLeaveForYear(int year, BigDecimal days) {
        if (days == null || days.compareTo(BigDecimal.ZERO) <= 0) {
            days = BigDecimal.valueOf(14);
        }

        // Lấy danh sách nhân viên đang làm việc và thử việc
        List<EmployeeStatus> activeStatuses = List.of(
                EmployeeStatus.WORKING,
                EmployeeStatus.PROBATION);

        List<Employee> activeEmployees = employeeRepository.findByStatusIn(activeStatuses);
        for (Employee employee : activeEmployees) {
            accrue(employee, year, days, "Cấp quỹ phép năm " + year);
        }
    }

    @Override
    public void initializeBalanceForEmployee(Employee employee, int year, BigDecimal days) {
        if (employee == null || employee.getId() == null) {
            return;
        }

        // Nếu đã có quỹ phép năm này cho nhân viên thì không ghi đè
        if (leaveBalanceRepository.findByEmployeeIdAndYear(employee.getId(), year).isPresent()) {
            return;
        }

        // Mặc định chuẩn Bộ luật Lao động: 12 ngày phép/năm
        BigDecimal grantDays = (days != null && days.compareTo(BigDecimal.ZERO) > 0) ? days : BigDecimal.valueOf(12);

        LeaveBalance balance = LeaveBalance.builder()
                .employee(employee)
                .year(year)
                .totalDays(grantDays)
                .usedDays(BigDecimal.ZERO)
                .pendingDays(BigDecimal.ZERO)
                .expiredDays(BigDecimal.ZERO)
                .build();

        balance = leaveBalanceRepository.save(balance);
        leaveBalanceTransactionService.createAccrualTransaction(
                balance,
                grantDays,
                "Khởi tạo quỹ phép năm " + year + " khi tiếp nhận nhân viên mới");
    }

    @Override
    @Transactional(readOnly = true)
    public com.kltn.school_hrm.module.leave.dto.response.LeaveBalanceResponse getBalanceByEmployeeAndYear(Long employeeId, int year) {
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndYear(employeeId, year)
                .orElse(null);

        if (balance == null) {
            Employee employee = employeeRepository.findById(employeeId).orElse(null);
            return com.kltn.school_hrm.module.leave.dto.response.LeaveBalanceResponse.builder()
                    .employeeId(employeeId)
                    .employeeCode(employee != null ? employee.getEmployeeCode() : null)
                    .employeeName(employee != null ? employee.getFullName() : null)
                    .year(year)
                    .totalDays(BigDecimal.ZERO)
                    .usedDays(BigDecimal.ZERO)
                    .pendingDays(BigDecimal.ZERO)
                    .expiredDays(BigDecimal.ZERO)
                    .remainingDays(BigDecimal.ZERO)
                    .build();
        }

        return mapToResponse(balance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.kltn.school_hrm.module.leave.dto.response.LeaveBalanceResponse> getAllBalancesByYear(int year) {
        return leaveBalanceRepository.findByYear(year).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private com.kltn.school_hrm.module.leave.dto.response.LeaveBalanceResponse mapToResponse(LeaveBalance balance) {
        return com.kltn.school_hrm.module.leave.dto.response.LeaveBalanceResponse.builder()
                .id(balance.getId())
                .employeeId(balance.getEmployee() != null ? balance.getEmployee().getId() : null)
                .employeeCode(balance.getEmployee() != null ? balance.getEmployee().getEmployeeCode() : null)
                .employeeName(balance.getEmployee() != null ? balance.getEmployee().getFullName() : null)
                .year(balance.getYear())
                .totalDays(balance.getTotalDays() != null ? balance.getTotalDays() : BigDecimal.ZERO)
                .usedDays(balance.getUsedDays() != null ? balance.getUsedDays() : BigDecimal.ZERO)
                .pendingDays(balance.getPendingDays() != null ? balance.getPendingDays() : BigDecimal.ZERO)
                .expiredDays(balance.getExpiredDays() != null ? balance.getExpiredDays() : BigDecimal.ZERO)
                .remainingDays(balance.getRemainingDays() != null ? balance.getRemainingDays() : BigDecimal.ZERO)
                .build();
    }
}
