package com.kltn.school_hrm.module.payroll.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.payroll.entity.TaxPolicy;

@Repository
public interface TaxPolicyRepository extends JpaRepository<TaxPolicy, Long> {

    Optional<TaxPolicy> findByCode(String code);

    /**
     * Tìm chính sách thuế đang active có ngày bắt đầu hiệu lực <= targetDate và còn hiệu lực.
     */
    @Query("""
            SELECT tp FROM TaxPolicy tp
            WHERE tp.isActive = true
              AND tp.effectiveFrom <= :targetDate
              AND (tp.effectiveTo IS NULL OR tp.effectiveTo >= :targetDate)
            ORDER BY tp.effectiveFrom DESC
            """)
    Optional<TaxPolicy> findActivePolicyForDate(@Param("targetDate") LocalDate targetDate);

    /**
     * Tìm chính sách thuế active mới nhất.
     */
    Optional<TaxPolicy> findFirstByIsActiveTrueOrderByEffectiveFromDesc();
}
