package com.kltn.school_hrm.module.employee.service.implement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.core.entity.User;
import com.kltn.school_hrm.module.core.repository.UserRepository;
import com.kltn.school_hrm.module.employee.dto.request.SalaryChangeRequest;
import com.kltn.school_hrm.module.employee.dto.response.SalaryHistoryResponse;
import com.kltn.school_hrm.module.employee.entity.Contract;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.employee.entity.SalaryHistory;
import com.kltn.school_hrm.module.employee.repository.ContractRepository;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.module.employee.repository.SalaryHistoryRepository;
import com.kltn.school_hrm.module.employee.service.SalaryHistoryService;
import com.kltn.school_hrm.shared.enums.Enums.ContractStatus;
import com.kltn.school_hrm.shared.enums.Enums.Currency;
import com.kltn.school_hrm.shared.enums.Enums.SalaryChangeReason;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;
import com.kltn.school_hrm.shared.exception.custom.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Triển khai quản lý lịch sử điều chỉnh lương và cập nhật mức lương hợp đồng.
 * Đảm bảo mọi thay đổi về tiền lương đều được lưu vết pháp lý và gắn với quyết định phê duyệt.
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class SalaryHistoryServiceImpl implements SalaryHistoryService {

    private final SalaryHistoryRepository salaryHistoryRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Tiếp nhận yêu cầu điều chỉnh lương qua DTO và chuyển đổi sang nghiệp vụ lưu vết.
     * Cung cấp hợp đồng giao tiếp chuẩn hóa với tầng REST Controller.
     */
    @Override
    public SalaryHistoryResponse changeSalary(Long contractId, SalaryChangeRequest request, Long approvedByUserId) {
        SalaryHistory sh = changeSalary(
                contractId,
                request.getNewSalary(),
                request.getCurrency(),
                request.getEffectiveDate(),
                request.getDecisionNumber(),
                request.getReason(),
                request.getNote(),
                approvedByUserId);
        return mapToResponse(sh);
    }

    /**
     * Thực hiện kiểm tra tính hợp lệ của đợt tăng/giảm lương và lưu bản ghi lịch sử.
     * Cần cập nhật trực tiếp vào grossSalary của hợp đồng để các chu kỳ tính lương kế tiếp lấy được giá trị mới nhất.
     */
    @Override
    public SalaryHistory changeSalary(Long contractId,
            BigDecimal newSalary,
            Currency currency,
            LocalDate effectiveDate,
            String decisionNumber,
            SalaryChangeReason reason,
            String note,
            Long approvedBy) {

        // 1. Chỉ cho phép điều chỉnh lương đối với hợp đồng đang ACTIVE để tránh sai lệch dữ liệu đã kết thúc
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hợp đồng lao động: ID " + contractId));

        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new BusinessException("Chỉ có thể điều chỉnh lương cho hợp đồng đang có hiệu lực (ACTIVE)");
        }

        BigDecimal oldSalary = contract.getGrossSalary();

        // 2. Kiểm tra tính hợp lệ của mức lương mới so với mức lương hiện hành
        if (newSalary == null || newSalary.signum() <= 0) {
            throw new BusinessException("Mức lương mới phải lớn hơn 0");
        }

        if (oldSalary != null && oldSalary.compareTo(newSalary) == 0) {
            throw new BusinessException("Mức lương mới phải khác với mức lương hiện tại (" + oldSalary + ")");
        }

        User approver = null;
        if (approvedBy != null) {
            approver = userRepository.findById(approvedBy)
                    .orElseThrow(() -> new NotFoundException("Tài khoản người duyệt không tồn tại: ID " + approvedBy));
        }

        // 3. Kiểm tra ngày hiệu lực phải nằm trong khoảng thời gian hợp đồng và sau lần điều chỉnh trước
        SalaryHistory latest = salaryHistoryRepository.findFirstByContractIdOrderByEffectiveDateDesc(contractId).orElse(null);
        LocalDate prevEffectiveDate = (latest != null) ? latest.getEffectiveDate() : null;

        if (!isValidEffectiveDate(effectiveDate, contract.getStartDate(), contract.getEndDate(), prevEffectiveDate)) {
            throw new BusinessException("Ngày hiệu lực không hợp lệ. Ngày hiệu lực phải nằm trong thời hạn hợp đồng và không được trước lần điều chỉnh lương gần nhất (" 
                    + (prevEffectiveDate != null ? prevEffectiveDate : contract.getStartDate()) + ")");
        }

        // 4. Khởi tạo bản ghi lưu vết lịch sử lương
        SalaryHistory newHistory = SalaryHistory.builder()
                .contract(contract)
                .oldGrossSalary(oldSalary)
                .newGrossSalary(newSalary)
                .currency(currency != null ? currency : contract.getCurrency())
                .effectiveDate(effectiveDate)
                .decisionNumber(decisionNumber)
                .reason(reason)
                .note(note)
                .approvedBy(approver)
                .build();

        // 5. Cập nhật mức lương mới vào hợp đồng để đồng bộ dữ liệu nguồn
        contract.changeSalary(newSalary);
        if (currency != null) {
            contract.setCurrency(currency);
        }
        contractRepository.save(contract);

        SalaryHistory savedHistory = salaryHistoryRepository.save(newHistory);
        log.info("Đã điều chỉnh lương hợp đồng {}: {} -> {} (áp dụng từ {})",
                contract.getContractNumber(), oldSalary, newSalary, effectiveDate);

        return savedHistory;
    }

    /**
     * Truy xuất toàn bộ lịch sử biến động lương của hợp đồng theo thứ tự ngày hiệu lực giảm dần.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SalaryHistoryResponse> getHistoryByContract(Long contractId) {
        if (!contractRepository.existsById(contractId)) {
            throw new NotFoundException("Không tìm thấy hợp đồng lao động: ID " + contractId);
        }
        return salaryHistoryRepository.findByContractIdOrderByEffectiveDateDesc(contractId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy bản ghi điều chỉnh lương gần nhất để phục vụ đối soát và gợi ý dữ liệu.
     */
    @Override
    @Transactional(readOnly = true)
    public SalaryHistoryResponse getLatest(Long contractId) {
        return salaryHistoryRepository.findFirstByContractIdOrderByEffectiveDateDesc(contractId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    /**
     * Xác thực ngày hiệu lực nhằm ngăn ngừa tình trạng ngày quyết định nằm ngoài khung thời gian của hợp đồng.
     */
    private boolean isValidEffectiveDate(LocalDate effectiveDate, LocalDate contractStartDate,
            LocalDate contractEndDate, LocalDate prevEffectiveDate) {

        if (effectiveDate == null) {
            return false;
        }

        // Ngày hiệu lực không được trước ngày bắt đầu hợp đồng
        if (effectiveDate.isBefore(contractStartDate)) {
            return false;
        }

        // Nếu hợp đồng có ngày kết thúc, ngày hiệu lực không được sau ngày kết thúc
        if (contractEndDate != null && effectiveDate.isAfter(contractEndDate)) {
            return false;
        }

        // Ngày hiệu lực không được trước ngày áp dụng của lần điều chỉnh trước đó
        if (prevEffectiveDate != null && effectiveDate.isBefore(prevEffectiveDate)) {
            return false;
        }

        return true;
    }

    /**
     * Chuyển đổi Entity sang Response DTO và bổ sung tên hiển thị người duyệt cùng chênh lệch lương.
     */
    private SalaryHistoryResponse mapToResponse(SalaryHistory sh) {
        if (sh == null) return null;

        BigDecimal diff = null;
        if (sh.getNewGrossSalary() != null && sh.getOldGrossSalary() != null) {
            diff = sh.getNewGrossSalary().subtract(sh.getOldGrossSalary());
        }

        String approverName = null;
        if (sh.getApprovedBy() != null) {
            approverName = employeeRepository.findByUserId(sh.getApprovedBy().getId())
                    .map(Employee::getFullName)
                    .orElse(sh.getApprovedBy().getUsername());
        }

        Contract contract = sh.getContract();
        Employee employee = (contract != null) ? contract.getEmployee() : null;

        return SalaryHistoryResponse.builder()
                .id(sh.getId())
                .contractId(contract != null ? contract.getId() : null)
                .contractNumber(contract != null ? contract.getContractNumber() : null)
                .employeeId(employee != null ? employee.getId() : null)
                .employeeName(employee != null ? employee.getFullName() : null)
                .employeeCode(employee != null ? employee.getEmployeeCode() : null)
                .oldGrossSalary(sh.getOldGrossSalary())
                .newGrossSalary(sh.getNewGrossSalary())
                .difference(diff)
                .currency(sh.getCurrency())
                .effectiveDate(sh.getEffectiveDate())
                .decisionNumber(sh.getDecisionNumber())
                .reason(sh.getReason())
                .reasonLabel(sh.getReason() != null ? sh.getReason().getLabel() : null)
                .note(sh.getNote())
                .approvedById(sh.getApprovedBy() != null ? sh.getApprovedBy().getId() : null)
                .approvedByName(approverName)
                .createdAt(sh.getCreatedAt())
                .build();
    }
}
