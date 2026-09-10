package com.kltn.school_hrm.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.attendance.LeaveApproval;
import com.kltn.school_hrm.enums.Enums.ApprovalStatus;

@Repository
public interface LeaveApprovalRepository extends JpaRepository<LeaveApproval, Long> {

    @Query("SELECT la FROM LeaveApproval la WHERE la.approver.id = :approverId AND la.status = :status")
    List<LeaveApproval> findByApproverIdAndStatus(@Param("approverId") Long approverId, @Param("status") ApprovalStatus status);

    @Modifying
    @Query("UPDATE LeaveApproval la SET la.approver.id = :newApproverId WHERE la.approver.id = :oldApproverId AND la.status = 'PENDING'")
    int transferPendingApprovals(@Param("oldApproverId") Long oldApproverId, @Param("newApproverId") Long newApproverId);
}

