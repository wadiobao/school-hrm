package com.kltn.school_hrm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.approval.ApprovalAction;

@Repository
public interface ApprovalActionRepository extends JpaRepository<ApprovalAction, Long> {

    List<ApprovalAction> findByApprovalRequestIdOrderByActionAtAsc(Long approvalRequestId);
}
