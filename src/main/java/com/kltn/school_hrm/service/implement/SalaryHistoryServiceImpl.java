package com.kltn.school_hrm.service.implement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.entity.core.User;
import com.kltn.school_hrm.entity.employee.Contract;
import com.kltn.school_hrm.entity.history.SalaryHistory;
import com.kltn.school_hrm.enums.Enums.ContractStatus;
import com.kltn.school_hrm.enums.Enums.Currency;
import com.kltn.school_hrm.enums.Enums.SalaryChangeReason;
import com.kltn.school_hrm.exception.custom.BusinessException;
import com.kltn.school_hrm.exception.custom.NotFoundException;
import com.kltn.school_hrm.repository.ContractRepository;
import com.kltn.school_hrm.repository.SalaryHistoryRepository;
import com.kltn.school_hrm.repository.UserRepository;
import com.kltn.school_hrm.service.SalaryHistoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class SalaryHistoryServiceImpl implements SalaryHistoryService {

    private final SalaryHistoryRepository salaryHistoryRepository;

    private final ContractRepository contractRepository;

    private final UserRepository userRepository;

    @Override
    public SalaryHistory changeSalary(Long contractId,
            BigDecimal newSalary,
            Currency currency,
            LocalDate effectiveDate,
            String decisionNumber,
            SalaryChangeReason reason,
            Long approvedBy) {

        // 1. Kiểm tra hợp đồng tồn tại và đang hoạt động
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hợp đồng"));

        // Kiểm tra contract status là active
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new BusinessException("Không thể thay đổi lương của hợp đồng không hoạt động");
        }

        BigDecimal oldSalary = contract.getGrossSalary();

        // 2. Kiểm tra newSalary không âm, lớn hơn 0 và khác oldSalary
        if (newSalary == null || newSalary.signum() <= 0) {
            throw new BusinessException(
                    "Lương phải lớn hơn 0");
        }

        if (oldSalary.compareTo(newSalary) == 0) {
            throw new BusinessException(
                    "Lương mới phải khác lương cũ");
        }

        User approver = userRepository.findById(approvedBy)
                .orElseThrow(() -> new NotFoundException("Người duyệt không tồn tại"));

        // Lấy ngày hiệu lực của lịch sử lương trước đó
        SalaryHistory latest = getLatest(contractId);
        LocalDate prevEffectiveDate = null;
        if (latest != null) {
            prevEffectiveDate = latest.getEffectiveDate();
        }

        //3. Kiểm tra ngày hiệu lực của lịch sử lương phải lớn hơn ngày bắt đầu hợp đồng
        if (isValidEffectiveDate(effectiveDate, contract.getStartDate(), contract.getEndDate(),
                prevEffectiveDate)) {
            throw new BusinessException("Ngày hiệu lực không hợp lệ");
        }

        // 4. Tạo bản ghi lịch sử lương mới
        SalaryHistory newHistory = SalaryHistory.builder()
                .contract(contract)
                .oldGrossSalary(oldSalary)
                .newGrossSalary(newSalary)
                .currency(currency)
                .effectiveDate(effectiveDate)
                .decisionNumber(decisionNumber)
                .reason(reason)
                .approvedBy(approver)
                .build();

        // 5. Cập nhật lương mới vào hợp đồng
        contract.changeSalary(newSalary);

        return salaryHistoryRepository.save(newHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalaryHistory> getHistoryByContract(Long contractId) {
        return salaryHistoryRepository.findByContractIdOrderByEffectiveDateDesc(contractId);
    }

    @Override
    @Transactional(readOnly = true)
    public SalaryHistory getLatest(Long contractId) {
        return salaryHistoryRepository.findFirstByContractIdOrderByEffectiveDateDesc(contractId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy lịch sử lương"));
    }

    private boolean isValidEffectiveDate(LocalDate effectiveDate, LocalDate contractStartDate,
            LocalDate contractEndDate, LocalDate prevEffectiveDate) {

        // Ngày hiệu lực phải lớn hơn ngày bắt đầu hợp đồng
        if (effectiveDate.isBefore(contractStartDate)) {
            return false;
        }

        // Nếu hợp đồng có ngày kết thúc, ngày hiệu lực phải nhỏ hơn ngày kết thúc
        if (contractEndDate != null && effectiveDate.isAfter(contractEndDate)) {
            return false;
        }

        // Ngày hiệu lực phải lớn hơn ngày hiệu lực của lịch sử lương trước đó
        if (prevEffectiveDate != null && effectiveDate.isBefore(prevEffectiveDate)) {
            return false;
        }

        return true;
    }
}
