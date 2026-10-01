package com.kltn.school_hrm.module.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kltn.school_hrm.module.employee.entity.EmployeeStatusHistory;

public interface EmployeeStatusHistoryRepository extends JpaRepository<EmployeeStatusHistory, Long> {

}
