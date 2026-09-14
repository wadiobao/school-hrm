package com.kltn.school_hrm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.approval.ApprovalRequestStep;
import com.kltn.school_hrm.enums.Enums.ApprovalStatus;

@Repository
public interface ApprovalRequestStepRepository extends JpaRepository<ApprovalRequestStep, Long> {

    List<ApprovalRequestStep> findByApprovalRequestIdOrderByLevelOrderAsc(Long approvalRequestId);

    Optional<ApprovalRequestStep> findByApprovalRequestIdAndLevelOrder(Long approvalRequestId, Integer levelOrder);

    @Query("SELECT s FROM ApprovalRequestStep s WHERE s.assignedApprover.id = :approverId AND s.status = :status")
    List<ApprovalRequestStep> findByAssignedApproverIdAndStatus(@Param("approverId") Long approverId, @Param("status") ApprovalStatus status);
}
