package com.kltn.school_hrm.job;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kltn.school_hrm.dto.response.LeaveResponse;
import com.kltn.school_hrm.service.LeaveService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeaveRequestOverdueJob {

    private final LeaveService leaveService;

    /**
     * Tự động chạy mỗi giờ (cron = "0 0 * * * ?")
     * để quét các đơn nghỉ phép quá hạn (waiting time > totalDays/2),
     * tự động chuyển sang status OVERDUE và giải phóng ngày phép pending.
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void scanAndProcessOverdueLeaveRequests() {
        log.info("Bắt đầu Scheduled Job: Quét các đơn nghỉ phép quá hạn phê duyệt...");
        try {
            List<LeaveResponse> processed = leaveService.processOverdueLeaveRequests();
            if (!processed.isEmpty()) {
                log.info("Đã tự động chuyển trạng thái OVERDUE cho {} đơn nghỉ phép: {}",
                        processed.size(),
                        processed.stream().map(LeaveResponse::getId).toList());
            } else {
                log.info("Không có đơn nghỉ phép nào bị quá hạn.");
            }
        } catch (Exception e) {
            log.error("Lỗi khi chạy scheduled job quét đơn nghỉ phép quá hạn", e);
        }
    }
}
