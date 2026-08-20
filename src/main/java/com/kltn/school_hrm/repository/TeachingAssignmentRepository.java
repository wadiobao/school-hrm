package com.kltn.school_hrm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.teaching.TeachingAssignment;
import com.kltn.school_hrm.enums.Enums.Curriculum;

@Repository
public interface TeachingAssignmentRepository extends JpaRepository<TeachingAssignment, Long> {

	List<TeachingAssignment> findByTeacherId(Long teacherId);
	List<TeachingAssignment> findByTeacherIdAndCurriculum(Long teacherId, Curriculum curriculum);
}
