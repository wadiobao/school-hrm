package com.kltn.school_hrm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.approval.ApprovalLevel;

@Repository
public interface ApprovalLevelRepository extends JpaRepository<ApprovalLevel, Long> {

    List<ApprovalLevel> findByPolicyIdOrderByLevelOrderAsc(Long policyId);
}
