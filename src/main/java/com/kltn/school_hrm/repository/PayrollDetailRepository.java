package com.kltn.school_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.payroll.PayrollDetail;

@Repository
public interface PayrollDetailRepository extends JpaRepository<PayrollDetail, Long>{

}
