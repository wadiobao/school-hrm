package com.kltn.school_hrm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.approval.ApprovalPolicy;

@Repository
public interface ApprovalPolicyRepository extends JpaRepository<ApprovalPolicy, Long> {

    Optional<ApprovalPolicy> findFirstByBusinessTypeAndIsActiveTrueOrderByVersionDesc(String businessType);

    Optional<ApprovalPolicy> findByCodeAndVersion(String code, Integer version);
}
