package com.kltn.school_hrm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.teaching.TeachingLog;
import com.kltn.school_hrm.enums.Enums.RequestStatus;

@Repository
public interface TeachingLogRepository extends JpaRepository<TeachingLog, Long> {

	List<TeachingLog> findByAssignmentId(Long assignmentId);
	List<TeachingLog> findByActualTeacherId(Long teacherId);
	List<TeachingLog> findByStatus(RequestStatus status);
}
