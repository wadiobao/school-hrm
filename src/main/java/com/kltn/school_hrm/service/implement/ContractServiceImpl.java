package com.kltn.school_hrm.service.implement;

import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.dto.request.ContractRequest;
import com.kltn.school_hrm.dto.response.ContractResponse;
import com.kltn.school_hrm.entity.core.Position;
import com.kltn.school_hrm.entity.employee.Contract;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.enums.Enums.ContractStatus;
import com.kltn.school_hrm.enums.Enums.ContractType;
import com.kltn.school_hrm.enums.Enums.EmployeeStatus;
import com.kltn.school_hrm.exception.custom.BusinessException;
import com.kltn.school_hrm.exception.custom.NotFoundException;
import com.kltn.school_hrm.repository.ContractRepository;
import com.kltn.school_hrm.repository.EmployeeRepository;
import com.kltn.school_hrm.repository.PositionRepository;
import com.kltn.school_hrm.service.ContractService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;

    @Override
    @Transactional
    public ContractResponse createInitialContract(ContractRequest request) {
        if (request.getType() != ContractType.PROBATION) {
            throw new BusinessException("Initial contract must be PROBATION");
        }
        return createContract(request);
    }

    @Override
    @Transactional
    public ContractResponse createContract(ContractRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new NotFoundException("Nhân viên không tồn tại"));

        Position position = positionRepository.findById(request.getPositionId())
                .orElseThrow(() -> new NotFoundException("Vị trí không tồn tại"));

        // 1. Validate logic loại hợp đồng
        if (request.getType() == ContractType.INDEFINITE_TERM) {
            request.setEndDate(null); // Bắt buộc null nếu là vô thời hạn
        } else if (request.getEndDate() == null || !request.getEndDate().isAfter(request.getStartDate())) {
            throw new BusinessException("Ngày kết thúc hợp đồng không hợp lệ!");
        }

        // 2. Kiểm tra overlap hợp đồng
        List<Contract> activeContracts = contractRepository.findByEmployeeIdAndStatus(employee.getId(),
                ContractStatus.ACTIVE);
        for (Contract existing : activeContracts) {
            // Nếu hợp đồng hiện tại là vô thời hạn, không thể tạo hợp đồng mới trùng hoặc
            // sau nó
            if (existing.getEndDate() == null) {
                if (!request.getStartDate().isBefore(existing.getStartDate())) {
                    throw new BusinessException(
                            "Nhân viên này đang có hợp đồng vô thời hạn, không thể tạo hợp đồng mới.");
                }
            } else {
                // Nếu contract mới bắt đầu trước khi contract cũ kết thúc
                if (!request.getStartDate().isAfter(existing.getEndDate())) {
                    throw new BusinessException("Thời gian hợp đồng bị trùng lặp với hợp đồng đang có hiệu lực!");
                }
            }
        }

        if (request.getType() == ContractType.PROBATION) {
            employee.setStatus(EmployeeStatus.PROBATION);
        } else if (employee.getStatus() == EmployeeStatus.PROBATION && request.getType() != ContractType.PROBATION) {
            // Optional: automatically transition to WORKING if a non-probation contract is
            // created
            employee.setStatus(EmployeeStatus.WORKING);
        }

        // 3. Tạo mới hợp đồng
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
        return mapToResponse(savedContract);
    }

    @Override
    public ContractResponse updateContract(ContractRequest request) {
        return null;
    }

    @Override
    public ContractResponse deleteConract(ContractRequest request) {
        return null;
    }

    @Override
    @Transactional
    public void terminate(Long contractId) {
        Contract contract = getContract(contractId);
        contract.terminate();
    }

    @Override
    @Transactional
    public void expire(Long contractId) {
        Contract contract = getContract(contractId);
        contract.expire();
    }

    @Override
    @Transactional
    public ContractResponse renew(Long contractId, ContractRequest request) {
        // Renew typically means creating a new contract that follows the current one
        Contract existingContract = getContract(contractId);

        // ensure the request startDate is after existing endDate
        if (existingContract.getEndDate() == null) {
            throw new BusinessException("Cannot renew an indefinite contract");
        }

        if (!request.getStartDate().isAfter(existingContract.getEndDate())) {
            throw new BusinessException("Renewed contract start date must be after current contract end date");
        }

        // enforce same employee
        request.setEmployeeId(existingContract.getEmployee().getId());

        return createContract(request);
    }

    @Override
    public List<ContractResponse> getContractHistory(Long employeeId) {
        // Ensure employee exists before querying
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Nhân viên không tồn tại"));

        return contractRepository.findByEmployeeIdOrderByStartDateDesc(employeeId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Contract getContract(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Contract not found"));
    }

    private ContractResponse mapToResponse(Contract contract) {
        return ContractResponse.builder()
                .id(contract.getId())
                .contractNumber(contract.getContractNumber())
                .employee(new ContractResponse.EmployeeSummary(
                        contract.getEmployee().getId(),
                        contract.getEmployee().getEmployeeCode(),
                        contract.getEmployee().getFullName(),
                        contract.getEmployee().getEmail()))
                .positionName(contract.getPosition().getName())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .type(contract.getType())
                .status(contract.getStatus())
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
