package com.kltn.school_hrm.service.implement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.enums.Enums.EmployeeStatus;
import com.kltn.school_hrm.exception.custom.NotFoundException;
import com.kltn.school_hrm.repository.EmployeeRepository;
import com.kltn.school_hrm.service.EmployeeLifecycleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeLifecycleServiceImpl implements EmployeeLifecycleService {
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public void completeProbation(Long employeeId) {
        Employee employee = getEmployee(employeeId);

        employee.changeStatus(EmployeeStatus.WORKING);
    }

    @Override
    @Transactional
    public void suspend(Long employeeId) {
        Employee employee = getEmployee(employeeId);

        employee.changeStatus(EmployeeStatus.SUSPENDED);
    }

    @Override
    @Transactional
    public void resume(Long employeeId) {
        Employee employee = getEmployee(employeeId);

        employee.changeStatus(EmployeeStatus.WORKING);
    }

    @Override
    @Transactional
    public void resign(Long employeeId) {
        Employee employee = getEmployee(employeeId);

        employee.changeStatus(EmployeeStatus.RESIGNED);
    }

    @Override
    public void retire(Long employeeId) {
        Employee employee = getEmployee(employeeId);

        employee.changeStatus(EmployeeStatus.RETIRED);
    }

    private Employee getEmployee(Long employeeId) {

        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found"));
    }
}
