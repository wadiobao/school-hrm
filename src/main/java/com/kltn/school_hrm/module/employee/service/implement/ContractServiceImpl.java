package com.kltn.school_hrm.module.employee.service.implement;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.core.entity.Position;
import com.kltn.school_hrm.module.core.repository.PositionRepository;
import com.kltn.school_hrm.module.employee.dto.request.ContractRequest;
import com.kltn.school_hrm.module.employee.dto.response.ContractResponse;
import com.kltn.school_hrm.module.employee.entity.Contract;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.employee.entity.SalaryHistory;
import com.kltn.school_hrm.module.employee.repository.ContractRepository;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.module.employee.repository.SalaryHistoryRepository;
import com.kltn.school_hrm.module.employee.service.ContractService;
import com.kltn.school_hrm.shared.enums.Enums.ContractStatus;
import com.kltn.school_hrm.shared.enums.Enums.ContractType;
import com.kltn.school_hrm.shared.enums.Enums.EmployeeStatus;
import com.kltn.school_hrm.shared.enums.Enums.SalaryChangeReason;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;
import com.kltn.school_hrm.shared.exception.custom.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final SalaryHistoryRepository salaryHistoryRepository;

    @Override
    public ContractResponse createInitialContract(ContractRequest request) {
        if (request.getType() != ContractType.PROBATION) {
            throw new BusinessException("Hợp đồng khởi đầu cho nhân sự mới phải là Thử việc (PROBATION)");
        }
        return createContract(request);
    }

    @Override
    public ContractResponse createContract(ContractRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new NotFoundException("Nhân viên không tồn tại: ID " + request.getEmployeeId()));

        Position position = positionRepository.findById(request.getPositionId())
                .orElseThrow(() -> new NotFoundException("Vị trí/Chức vụ không tồn tại: ID " + request.getPositionId()));

        // 1. Kiểm tra trùng số hợp đồng
        if (contractRepository.existsByContractNumber(request.getContractNumber())) {
            throw new BusinessException("Số hợp đồng '" + request.getContractNumber() + "' đã tồn tại trên hệ thống!");
        }

        // 2. Validate logic thời hạn theo loại hợp đồng
        if (request.getType() == ContractType.INDEFINITE_TERM) {
            request.setEndDate(null); // Bắt buộc null nếu là vô thời hạn
        } else if (request.getEndDate() == null || !request.getEndDate().isAfter(request.getStartDate())) {
            throw new BusinessException("Ngày kết thúc hợp đồng phải sau ngày bắt đầu!");
        }

        // 3. Kiểm tra overlap với các hợp đồng ACTIVE khác của nhân viên
        List<Contract> activeContracts = contractRepository.findByEmployeeIdAndStatus(employee.getId(), ContractStatus.ACTIVE);
        for (Contract existing : activeContracts) {
            if (existing.getEndDate() == null) {
                // Hợp đồng cũ là vô thời hạn
                if (!request.getStartDate().isBefore(existing.getStartDate())) {
                    throw new BusinessException("Nhân viên đang có hợp đồng vô thời hạn đang hiệu lực. Vui lòng chấm dứt hợp đồng cũ trước khi tạo hợp đồng mới.");
                }
            } else {
                // Hợp đồng cũ có ngày kết thúc: hợp đồng mới không được có thời gian bắt đầu trước khi hợp đồng cũ kết thúc
                if (!request.getStartDate().isAfter(existing.getEndDate())) {
                    throw new BusinessException("Thời gian hợp đồng mới (" + request.getStartDate() 
                            + ") bị trùng lặp với hợp đồng đang hiệu lực kết thúc ngày " + existing.getEndDate());
                }
            }
        }

        // 4. Đồng bộ trạng thái nhân viên theo loại hợp đồng
        if (request.getType() == ContractType.PROBATION) {
            employee.setStatus(EmployeeStatus.PROBATION);
        } else if (employee.getStatus() == EmployeeStatus.PROBATION) {
            employee.setStatus(EmployeeStatus.WORKING);
        }

        // 5. Tạo mới hợp đồng
        Contract contract = Contract.builder()
                .contractNumber(request.getContractNumber())
                .employee(employee)
                .position(position)
                .type(request.getType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .grossSalary(request.getGrossSalary())
                .currency(request.getCurrency())
                .housingAllowance(request.getHousingAllowance())
                .flightAllowance(request.getFlightAllowance())
                .relocationAllowance(request.getRelocationAllowance())
                .status(ContractStatus.ACTIVE)
                .build();

        contract.validate();
        Contract savedContract = contractRepository.save(contract);

        // Lưu vết mức lương khởi điểm vào SalaryHistory
        SalaryHistory initialHistory = SalaryHistory.builder()
                .contract(savedContract)
                .oldGrossSalary(null)
                .newGrossSalary(savedContract.getGrossSalary())
                .currency(savedContract.getCurrency())
                .effectiveDate(savedContract.getStartDate())
                .decisionNumber(savedContract.getContractNumber())
                .reason(SalaryChangeReason.ADJUSTMENT)
                .note("Mức lương khởi điểm theo Hợp đồng " + savedContract.getContractNumber())
                .build();
        salaryHistoryRepository.save(initialHistory);

        log.info("Tạo mới hợp đồng {} cho nhân viên {}", savedContract.getContractNumber(), employee.getFullName());
        return mapToResponse(savedContract);
    }

    @Override
    public ContractResponse renew(Long contractId, ContractRequest request) {
        // Renew = Tạo hợp đồng mới kế tiếp, không ghi đè (overwrite) hợp đồng cũ
        Contract existingContract = getContract(contractId);

        if (existingContract.getEndDate() == null) {
            throw new BusinessException("Hợp đồng vô thời hạn không thể gia hạn. Vui lòng chấm dứt nếu cần thay đổi.");
        }

        // Kiểm tra ngày bắt đầu của hợp đồng gia hạn phải sau ngày kết thúc của hợp đồng cũ
        if (request.getStartDate() == null || !request.getStartDate().isAfter(existingContract.getEndDate())) {
            throw new BusinessException("Ngày bắt đầu của hợp đồng gia hạn phải sau ngày kết thúc của hợp đồng cũ (" 
                    + existingContract.getEndDate() + ")");
        }

        // Gán cố định employeeId theo hợp đồng đang gia hạn
        request.setEmployeeId(existingContract.getEmployee().getId());

        // Nếu request không chọn lại position thì kế thừa position cũ
        if (request.getPositionId() == null) {
            request.setPositionId(existingContract.getPosition().getId());
        }

        log.info("Thực hiện gia hạn hợp đồng từ hợp đồng cũ {} cho nhân viên {}", 
                existingContract.getContractNumber(), existingContract.getEmployee().getFullName());

        return createContract(request);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractResponse getContractById(Long contractId) {
        return mapToResponse(getContract(contractId));
    }

    @Override
    @Transactional(readOnly = true)
    public ContractResponse getCurrentContract(Long employeeId) {
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Nhân viên không tồn tại: ID " + employeeId));

        LocalDate today = LocalDate.now();
        List<Contract> activeContracts = contractRepository.findByEmployeeIdAndStatus(employeeId, ContractStatus.ACTIVE);

        // Ưu tiên hợp đồng ACTIVE đang bao trùm ngày hôm nay
        return activeContracts.stream()
                .filter(c -> !c.getStartDate().isAfter(today) && (c.getEndDate() == null || !c.getEndDate().isBefore(today)))
                .findFirst()
                .map(this::mapToResponse)
                .orElseGet(() -> {
                    // Nếu không có hợp đồng bao trùm hôm nay, lấy hợp đồng ACTIVE mới nhất
                    return activeContracts.stream()
                            .findFirst()
                            .map(this::mapToResponse)
                            .orElse(null);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> getContractHistory(Long employeeId) {
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Nhân viên không tồn tại: ID " + employeeId));

        return contractRepository.findByEmployeeIdOrderByStartDateDesc(employeeId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void terminate(Long contractId) {
        Contract contract = getContract(contractId);
        contract.terminate();
        contractRepository.save(contract);
        log.info("Chấm dứt hợp đồng {} của nhân viên {}", contract.getContractNumber(), contract.getEmployee().getFullName());
    }

    @Override
    public void expire(Long contractId) {
        Contract contract = getContract(contractId);
        contract.expire();
        contractRepository.save(contract);
        log.info("Đánh dấu hết hạn hợp đồng {}", contract.getContractNumber());
    }

    @Override
    public ContractResponse updateContract(Long id, ContractRequest request) {
        Contract contract = getContract(id);

        // Quy tắc bất biến: Nếu hợp đồng đã có hiệu lực (ngày bắt đầu <= hôm nay),
        // KHÔNG cho phép sửa các trường lịch sử cốt lõi để bảo toàn dữ liệu
        boolean isStarted = !contract.getStartDate().isAfter(LocalDate.now());
        if (isStarted && contract.getStatus() == ContractStatus.ACTIVE) {
            boolean coreFieldsChanged = 
                    (request.getGrossSalary() != null && request.getGrossSalary().compareTo(contract.getGrossSalary()) != 0) ||
                    (request.getType() != null && request.getType() != contract.getType()) ||
                    (request.getPositionId() != null && !request.getPositionId().equals(contract.getPosition().getId())) ||
                    (request.getStartDate() != null && !request.getStartDate().equals(contract.getStartDate()));

            if (coreFieldsChanged) {
                throw new BusinessException("Hợp đồng này đang có hiệu lực. Để thay đổi Lương, Chức vụ hoặc Thời hạn, vui lòng tạo/gia hạn Hợp đồng mới để bảo toàn lịch sử.");
            }
        }

        // Kiểm tra số hợp đồng không trùng với hợp đồng khác
        if (request.getContractNumber() != null && contractRepository.existsByContractNumberAndIdNot(request.getContractNumber(), id)) {
            throw new BusinessException("Số hợp đồng '" + request.getContractNumber() + "' đã được sử dụng bởi hợp đồng khác!");
        }

        if (request.getContractNumber() != null) contract.setContractNumber(request.getContractNumber());
        if (!isStarted) {
            // Hợp đồng chưa có hiệu lực mới cho phép chỉnh sửa toàn diện
            if (request.getGrossSalary() != null) contract.setGrossSalary(request.getGrossSalary());
            if (request.getType() != null) contract.setType(request.getType());
            if (request.getStartDate() != null) contract.setStartDate(request.getStartDate());
            if (request.getEndDate() != null) contract.setEndDate(request.getEndDate());
            if (request.getPositionId() != null) {
                Position pos = positionRepository.findById(request.getPositionId())
                        .orElseThrow(() -> new NotFoundException("Vị trí không tồn tại"));
                contract.setPosition(pos);
            }
        }

        if (request.getHousingAllowance() != null) contract.setHousingAllowance(request.getHousingAllowance());
        if (request.getFlightAllowance() != null) contract.setFlightAllowance(request.getFlightAllowance());
        if (request.getRelocationAllowance() != null) contract.setRelocationAllowance(request.getRelocationAllowance());
        if (request.getCurrency() != null) contract.setCurrency(request.getCurrency());

        contract.validate();
        return mapToResponse(contractRepository.save(contract));
    }

    @Override
    public void deleteContract(Long id) {
        Contract contract = getContract(id);

        // Không cho phép xóa hợp đồng cũ trong lịch sử
        if (contract.getStatus() == ContractStatus.EXPIRED || contract.getStatus() == ContractStatus.TERMINATED) {
            throw new BusinessException("Không thể xóa hợp đồng đã hết hạn hoặc đã chấm dứt thuộc lịch sử nhân sự!");
        }

        // Nếu hợp đồng đang ACTIVE và đã có thời gian hoạt động, yêu cầu dùng chấm dứt (terminate)
        if (contract.getStatus() == ContractStatus.ACTIVE && !contract.getStartDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Hợp đồng đã có hiệu lực không thể xóa. Vui lòng sử dụng tính năng Chấm dứt hợp đồng (Terminate) để bảo toàn lịch sử tính lương.");
        }

        contractRepository.delete(contract);
        log.info("Đã xóa hợp đồng nháp/chưa hiệu lực ID {}", id);
    }

    private Contract getContract(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Hợp đồng không tồn tại: ID " + id));
    }

    private ContractResponse mapToResponse(Contract contract) {
        LocalDate today = LocalDate.now();
        boolean isCurrent = contract.getStatus() == ContractStatus.ACTIVE
                && !contract.getStartDate().isAfter(today)
                && (contract.getEndDate() == null || !contract.getEndDate().isBefore(today));

        return ContractResponse.builder()
                .id(contract.getId())
                .contractNumber(contract.getContractNumber())
                .employee(new ContractResponse.EmployeeSummary(
                        contract.getEmployee().getId(),
                        contract.getEmployee().getEmployeeCode(),
                        contract.getEmployee().getFullName(),
                        contract.getEmployee().getEmail()))
                .positionId(contract.getPosition().getId())
                .positionName(contract.getPosition().getName())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .type(contract.getType())
                .status(contract.getStatus())
                .isCurrent(isCurrent)
                .grossSalary(contract.getGrossSalary())
                .currency(contract.getCurrency())
                .housingAllowance(contract.getHousingAllowance())
                .flightAllowance(contract.getFlightAllowance())
                .relocationAllowance(contract.getRelocationAllowance())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .build();
    }
}
