package com.kltn.school_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.payroll.Payroll;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

}
