package com.kltn.school_hrm.module.teaching.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.teaching.entity.TeachingAssignment;
import com.kltn.school_hrm.shared.enums.Enums.Curriculum;
import com.kltn.school_hrm.shared.enums.Enums.TeachingAssignmentStatus;

@Repository
public interface TeachingAssignmentRepository extends JpaRepository<TeachingAssignment, Long> {

    List<TeachingAssignment> findByTeacherId(Long teacherId);

    List<TeachingAssignment> findByTeacherIdAndStatus(Long teacherId, TeachingAssignmentStatus status);

    List<TeachingAssignment> findByTeacherIdAndCurriculum(Long teacherId, Curriculum curriculum);

    List<TeachingAssignment> findBySchoolClassId(Long schoolClassId);

    Optional<TeachingAssignment> findBySchoolClassIdAndSubjectId(Long schoolClassId, Long subjectId);

    boolean existsBySchoolClassIdAndSubjectId(Long schoolClassId, Long subjectId);

    boolean existsBySchoolClassIdAndSubjectIdAndIdNot(Long schoolClassId, Long subjectId, Long id);
}
