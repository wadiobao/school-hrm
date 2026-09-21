package com.kltn.school_hrm.service.implement;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.entity.leave.LeaveBalance;
import com.kltn.school_hrm.entity.leave.LeaveBalanceTransaction;
import com.kltn.school_hrm.entity.leave.LeaveRequest;
import com.kltn.school_hrm.enums.Enums;
import com.kltn.school_hrm.repository.LeaveBalanceTransactionRepository;
import com.kltn.school_hrm.service.LeaveBalanceTransactionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class LeaveBalanceTransactionServiceImpl implements LeaveBalanceTransactionService {

    private final LeaveBalanceTransactionRepository leaveBalanceTransactionRepository;

    @Override
    public LeaveBalanceTransaction createApprovedLeaveBalanceTransaction(LeaveBalance leaveBalance, BigDecimal days,
            LeaveRequest leaveRequest) {
        String reason = null;
        String referenceId = null;
        if (leaveRequest != null) {
            reason = leaveRequest.getReason();
            if (leaveRequest.getId() != null) {
                referenceId = leaveRequest.getId().toString();
            }
        }
        if (reason == null || reason.isBlank()) {
            reason = "Nghỉ phép năm";
        }

        LocalDate effectiveDate = (leaveRequest != null && leaveRequest.getStartDate() != null)
                ? leaveRequest.getStartDate()
                : LocalDate.now();

        LeaveBalanceTransaction transaction = LeaveBalanceTransaction.builder()
                .leaveBalance(leaveBalance)
                .type(Enums.LeaveBalanceTransactionType.LEAVE)
                .effectiveDate(effectiveDate)
                .reason(reason)
                .referenceId(referenceId)
                .amount(days.negate())
                .transactionKey(generateLeaveKey(leaveRequest.getId()))
                .build();
        return leaveBalanceTransactionRepository.save(transaction);
    }

    @Override
    public LeaveBalanceTransaction createAccrualTransaction(LeaveBalance leaveBalance, BigDecimal days, String reason) {
        LeaveBalanceTransaction transaction = LeaveBalanceTransaction.builder()
                .leaveBalance(leaveBalance)
                .type(Enums.LeaveBalanceTransactionType.ACCRUAL)
                .effectiveDate(LocalDate.of(leaveBalance.getYear(), 1, 1))
                .reason(reason != null && !reason.isBlank() ? reason : "Tích lũy ngày phép")
                .amount(days)
                .transactionKey(generateAccrualKey(leaveBalance.getId(), leaveBalance.getYear()))
                .build();
        return leaveBalanceTransactionRepository.save(transaction);
    }

    @Override
    public LeaveBalanceTransaction createAdjustmentTransaction(LeaveBalance leaveBalance, BigDecimal days,
            String reason) {
        LeaveBalanceTransaction transaction = LeaveBalanceTransaction.builder()
                .leaveBalance(leaveBalance)
                .type(Enums.LeaveBalanceTransactionType.ADJUSTMENT)
                .effectiveDate(LocalDate.now())
                .reason(reason != null && !reason.isBlank() ? reason : "Điều chỉnh ngày phép")
                .amount(days)
                .transactionKey(generateAdjustmentKey(leaveBalance.getId()))
                .build();
        return leaveBalanceTransactionRepository.save(transaction);
    }

    @Override
    public LeaveBalanceTransaction createExpirationTransaction(LeaveBalance leaveBalance, BigDecimal days,
            String reason) {
        LeaveBalanceTransaction transaction = LeaveBalanceTransaction.builder()
                .leaveBalance(leaveBalance)
                .type(Enums.LeaveBalanceTransactionType.EXPIRATION)
                .effectiveDate(LocalDate.of(leaveBalance.getYear(), 12, 31))
                .reason(reason != null && !reason.isBlank() ? reason : "Hết hạn ngày phép")
                .amount(days.negate())
                .transactionKey(generateExpirationKey(leaveBalance.getId(), leaveBalance.getYear()))
                .build();
        return leaveBalanceTransactionRepository.save(transaction);
    }

    public static String generateExpirationKey(
            Long balanceId,
            int year) {
        return "EXPIRATION:" + balanceId + ":" + year;
    }

    public static String generateLeaveKey(
            Long leaveRequestId) {
        return "LEAVE:" + leaveRequestId;
    }

    public static String generateAccrualKey(
            Long balanceId,
            int year) {
        return "ACCRUAL:" + balanceId + ":" + year;
    }

    public static String generateAdjustmentKey(
            Long balanceId) {
        return "ADJUSTMENT:" + balanceId;
    }

}
