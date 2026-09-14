package com.kltn.school_hrm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.approval.ApprovalRequest;
import com.kltn.school_hrm.enums.Enums.ApprovalStatus;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {

    Optional<ApprovalRequest> findByBusinessTypeAndBusinessId(String businessType, Long businessId);

    List<ApprovalRequest> findByStatus(ApprovalStatus status);

    List<ApprovalRequest> findByRequesterId(Long requesterId);
}
