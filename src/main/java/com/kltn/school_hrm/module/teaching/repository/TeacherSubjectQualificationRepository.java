package com.kltn.school_hrm.module.teaching.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.teaching.entity.TeacherSubjectQualification;
import com.kltn.school_hrm.shared.enums.Enums.SubjectStatus;

@Repository
public interface TeacherSubjectQualificationRepository extends JpaRepository<TeacherSubjectQualification, Long> {

    List<TeacherSubjectQualification> findByTeacherId(Long teacherId);

    List<TeacherSubjectQualification> findBySubjectId(Long subjectId);

    Optional<TeacherSubjectQualification> findByTeacherIdAndSubjectId(Long teacherId, Long subjectId);

    boolean existsByTeacherIdAndSubjectId(Long teacherId, Long subjectId);

    boolean existsByTeacherIdAndSubjectIdAndStatus(Long teacherId, Long subjectId, SubjectStatus status);
}

