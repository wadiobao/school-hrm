package com.kltn.school_hrm.service;

public interface EmployeeLifecycleService {
    void completeProbation(Long employeeId, String reason);

    void suspend(Long employeeId, String reason);

    void resume(Long employeeId, String reason);

    void resign(Long employeeId, String reason);

    void retire(Long employeeId, String reason);
}
