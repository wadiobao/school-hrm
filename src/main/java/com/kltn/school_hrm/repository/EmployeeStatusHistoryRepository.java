package com.kltn.school_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kltn.school_hrm.entity.history.EmployeeStatusHistory;

public interface EmployeeStatusHistoryRepository extends JpaRepository<EmployeeStatusHistory, Long> {

}
