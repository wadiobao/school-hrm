package com.kltn.school_hrm.job;

import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kltn.school_hrm.service.LeaveBalanceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeaveBalanceExpirationJob {

    private final LeaveBalanceService leaveBalanceService;

    /**
     * Tự động chạy vào 00:05 ngày 1 tháng 1 hàng năm (cron = "0 5 0 1 1 ?")
     * để quét và hết hạn toàn bộ số ngày phép còn lại của năm trước đó.
     */
    @Scheduled(cron = "0 5 0 1 1 ?")
    public void expirePreviousYearLeaveBalances() {
        int previousYear = LocalDate.now().getYear() - 1;
        log.info("Bắt đầu Scheduled Job: Hết hạn quỹ phép cho năm cũ {}", previousYear);
        try {
            leaveBalanceService.expireYear(previousYear);
            log.info("Hoàn thành Scheduled Job: Hết hạn quỹ phép năm {}", previousYear);
        } catch (Exception e) {
            log.error("Lỗi khi chạy job hết hạn quỹ phép năm {}", previousYear, e);
        }
    }

    /**
     * Tự động chạy vào 00:10 ngày 1 tháng 1 hàng năm (cron = "0 10 0 1 1 ?")
     * để tự động tích lũy/cấp 14 ngày phép năm mới cho tất cả nhân viên đang hoạt động.
     */
    @Scheduled(cron = "0 10 0 1 1 ?")
    public void accrueNewYearLeaveBalances() {
        int currentYear = LocalDate.now().getYear();
        java.math.BigDecimal defaultAnnualLeaveDays = java.math.BigDecimal.valueOf(14);
        log.info("Bắt đầu Scheduled Job: Cấp quỹ phép {} ngày cho năm mới {}", defaultAnnualLeaveDays, currentYear);
        try {
            leaveBalanceService.accrueAnnualLeaveForYear(currentYear, defaultAnnualLeaveDays);
            log.info("Hoàn thành Scheduled Job: Cấp quỹ phép năm {}", currentYear);
        } catch (Exception e) {
            log.error("Lỗi khi chạy job cấp quỹ phép năm {}", currentYear, e);
        }
    }
}

