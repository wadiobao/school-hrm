package com.kltn.school_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.attendance.LeaveRequest;
import com.kltn.school_hrm.enums.Enums.RequestStatus;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
	List<LeaveRequest> findByEmployeeId(Long employeeId);

	@Query("""
			    SELECT COUNT(lr) > 0
			    FROM LeaveRequest lr
			    WHERE lr.employee.id = :employeeId
			      AND lr.status IN :statuses
			      AND lr.startDate <= :newEnd
			      AND lr.endDate >= :newStart
			""")
	boolean existsOverlap(
			@Param("employeeId") Long employeeId,
			@Param("statuses") Collection<RequestStatus> statuses,
			@Param("newStart") LocalDate newStart,
			@Param("newEnd") LocalDate newEnd);
}
