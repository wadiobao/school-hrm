package com.kltn.school_hrm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.history.SalaryHistory;

@Repository
public interface SalaryHistoryRepository extends JpaRepository<SalaryHistory, Long> {

    List<SalaryHistory> findByContractIdOrderByEffectiveDateDesc(Long contractId);

    Optional<SalaryHistory> findFirstByContractIdOrderByEffectiveDateDesc(Long contractId);
}
