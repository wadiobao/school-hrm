package com.kltn.school_hrm.module.employee.service.implement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.employee.entity.Contract;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.employee.entity.EmployeeStatusHistory;
import com.kltn.school_hrm.shared.enums.Enums.EmployeeStatus;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;
import com.kltn.school_hrm.shared.exception.custom.NotFoundException;
import com.kltn.school_hrm.module.employee.repository.ContractRepository;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.module.employee.repository.EmployeeStatusHistoryRepository;
import com.kltn.school_hrm.module.employee.service.EmployeeLifecycleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class EmployeeLifecycleServiceImpl implements EmployeeLifecycleService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeStatusHistoryRepository employeeStatusHistoryRepository;
    private final ContractRepository contractRepository;

    @Override
    @Transactional
    public void completeProbation(Long employeeId, String reason) {
        Employee employee = getEmployee(employeeId);

        Contract probationContract = contractRepository.findActiveProbationContractByEmployeeId(employeeId)
                .orElseThrow(() -> new BusinessException(
                        "Employee does not have an active probation contract"));

        probationContract.terminate();

        contractRepository.save(probationContract);

        EmployeeStatus oldStatus = employee.getStatus();

        EmployeeStatus newStatus = EmployeeStatus.WORKING;

        employee.changeStatus(newStatus);

        EmployeeStatusHistory history = EmployeeStatusHistory.builder()
                .employee(employee)
                .fromStatus(oldStatus)
                .toStatus(newStatus)
                .reason(reason)
                .build();

        employeeStatusHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void suspend(Long employeeId, String reason) {
        Employee employee = getEmployee(employeeId);

        EmployeeStatus oldStatus = employee.getStatus();

        EmployeeStatus newStatus = EmployeeStatus.SUSPENDED;

        employee.changeStatus(newStatus);

        EmployeeStatusHistory history = EmployeeStatusHistory.builder()
                .employee(employee)
                .fromStatus(oldStatus)
                .toStatus(newStatus)
                .reason(reason)
                .build();

        employeeStatusHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void resume(Long employeeId, String reason) {
        Employee employee = getEmployee(employeeId);

        EmployeeStatus oldStatus = employee.getStatus();

        EmployeeStatus newStatus = EmployeeStatus.WORKING;

        employee.changeStatus(newStatus);

        EmployeeStatusHistory history = EmployeeStatusHistory.builder()
                .employee(employee)
                .fromStatus(oldStatus)
                .toStatus(newStatus)
                .reason(reason)
                .build();

        employeeStatusHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void resign(Long employeeId, String reason) {
        Employee employee = getEmployee(employeeId);

        EmployeeStatus oldStatus = employee.getStatus();

        EmployeeStatus newStatus = EmployeeStatus.RESIGNED;

        employee.changeStatus(newStatus);

        EmployeeStatusHistory history = EmployeeStatusHistory.builder()
                .employee(employee)
                .fromStatus(oldStatus)
                .toStatus(newStatus)
                .reason(reason)
                .build();

        employeeStatusHistoryRepository.save(history);
    }

    @Override
    public void retire(Long employeeId, String reason) {
        Employee employee = getEmployee(employeeId);

        EmployeeStatus oldStatus = employee.getStatus();

        EmployeeStatus newStatus = EmployeeStatus.RETIRED;

        employee.changeStatus(newStatus);

        EmployeeStatusHistory history = EmployeeStatusHistory.builder()
                .employee(employee)
                .fromStatus(oldStatus)
                .toStatus(newStatus)
                .reason(reason)
                .build();

        employeeStatusHistoryRepository.save(history);
    }

    private Employee getEmployee(Long employeeId) {

        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found"));
    }
}
