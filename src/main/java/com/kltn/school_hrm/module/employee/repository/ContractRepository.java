package com.kltn.school_hrm.module.employee.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.employee.entity.Contract;
import com.kltn.school_hrm.shared.enums.Enums.ContractStatus;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    boolean existsByEmployeeIdAndStatus(Long employeeId, ContractStatus status);

    List<Contract> findByEmployeeIdAndStatus(Long employeeId, ContractStatus status);

    List<Contract> findByEmployeeIdOrderByStartDateDesc(Long employeeId);

    List<Contract> findByStatusAndEndDateBefore(ContractStatus status, LocalDate date);

    @Query("SELECT c FROM Contract c WHERE c.employee.id = :employeeId " +
            "AND c.type = 'PROBATION' AND c.status = 'ACTIVE' " +
            "AND CURRENT_DATE BETWEEN c.startDate AND c.endDate")
    Optional<Contract> findActiveProbationContractByEmployeeId(Long employeeId);

    /** Tìm hợp đồng hiệu lực trong kỳ lương (ưu tiên ACTIVE, khới đầu trước ngày kết thúc kỳ) */
    @Query("""
        SELECT c FROM Contract c
        WHERE c.employee.id = :employeeId
          AND c.status = 'ACTIVE'
          AND c.startDate <= :periodEnd
          AND (c.endDate IS NULL OR c.endDate >= :periodStart)
        ORDER BY c.startDate DESC
    """)
    Optional<Contract> findActiveContractForPeriod(
            @org.springframework.data.repository.query.Param("employeeId") Long employeeId,
            @org.springframework.data.repository.query.Param("periodStart") LocalDate periodStart,
            @org.springframework.data.repository.query.Param("periodEnd") LocalDate periodEnd);
}
