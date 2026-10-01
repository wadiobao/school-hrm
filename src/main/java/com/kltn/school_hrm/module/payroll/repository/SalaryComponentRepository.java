package com.kltn.school_hrm.module.payroll.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.payroll.entity.SalaryComponent;

@Repository
public interface SalaryComponentRepository extends JpaRepository<SalaryComponent, Long> {
    boolean existsByCode(String code);
    Optional<SalaryComponent> findByCode(String code);
}
