package com.kltn.school_hrm.job;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kltn.school_hrm.entity.employee.Contract;
import com.kltn.school_hrm.enums.Enums.ContractStatus;
import com.kltn.school_hrm.repository.ContractRepository;
import com.kltn.school_hrm.service.ContractService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContractExpirationJob {

    private final ContractRepository contractRepository;
    private final ContractService contractService;

    // Chạy vào 00:00 mỗi ngày
    @Scheduled(cron = "0 0 0 * * ?")
    public void expireContracts() {
        log.info("Bắt đầu job kiểm tra và expire hợp đồng...");
        LocalDate today = LocalDate.now();
        List<Contract> contractsToExpire = contractRepository.findByStatusAndEndDateBefore(ContractStatus.ACTIVE, today);
        
        int count = 0;
        for (Contract contract : contractsToExpire) {
            try {
                contractService.expire(contract.getId());
                count++;
            } catch (Exception e) {
                log.error("Lỗi khi expire hợp đồng có ID: {}", contract.getId(), e);
            }
        }
        
        log.info("Hoàn thành job expire hợp đồng. Số lượng hợp đồng đã expire: {}", count);
    }
}
