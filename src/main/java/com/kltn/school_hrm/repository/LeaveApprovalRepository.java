package com.kltn.school_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.attendance.LeaveApproval;

@Repository
public interface LeaveApprovalRepository extends JpaRepository<LeaveApproval, Long> {

}
