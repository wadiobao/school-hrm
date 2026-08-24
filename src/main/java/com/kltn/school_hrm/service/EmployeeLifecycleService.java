package com.kltn.school_hrm.service;

public interface EmployeeLifecycleService {
    void completeProbation(Long employeeId);

    void suspend(Long employeeId);

    void resume(Long employeeId);

    void resign(Long employeeId);

    void retire(Long employeeId);
}
