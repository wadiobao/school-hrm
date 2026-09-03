package com.kltn.school_hrm.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.employee.Contract;
import com.kltn.school_hrm.enums.Enums.ContractStatus;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    boolean existsByEmployeeIdAndStatus(Long employeeId, ContractStatus status);

    List<Contract> findByEmployeeIdAndStatus(Long employeeId, ContractStatus status);

    List<Contract> findByStatusAndEndDateBefore(ContractStatus status, LocalDate date);

    @Query("SELECT c FROM Contract c WHERE c.employee.id = :employeeId " +
            "AND c.type = 'PROBATION' AND c.status = 'ACTIVE' " +
            "AND CURRENT_DATE BETWEEN c.startDate AND c.endDate")
    Optional<Contract> findActiveProbationContractByEmployeeId(Long employeeId);
}
