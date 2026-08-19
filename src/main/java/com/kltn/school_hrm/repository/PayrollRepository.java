package com.kltn.school_hrm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.payroll.Payroll;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

	List<Payroll> findByEmployeeId(Long employeeId);
	Optional<Payroll> findByEmployeeIdAndMonthAndYear(Long employeeId, Integer month, Integer year);
	List<Payroll> findByMonthAndYear(Integer month, Integer year);
	boolean existsByEmployeeIdAndMonthAndYear(Long employeeId, Integer month, Integer year);
}
