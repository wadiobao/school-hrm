package com.kltn.school_hrm.module.academic.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.academic.entity.ClassSubject;

@Repository
public interface ClassSubjectRepository extends JpaRepository<ClassSubject, Long> {

    List<ClassSubject> findBySchoolClassId(Long schoolClassId);

    List<ClassSubject> findBySchoolClassIdAndSemesterId(Long schoolClassId, Long semesterId);

    List<ClassSubject> findBySemesterId(Long semesterId);

    Optional<ClassSubject> findBySchoolClassIdAndSubjectIdAndSemesterId(Long schoolClassId, Long subjectId, Long semesterId);

    boolean existsBySchoolClassIdAndSubjectIdAndSemesterId(Long schoolClassId, Long subjectId, Long semesterId);

    boolean existsBySchoolClassIdAndSubjectIdAndSemesterIdAndIdNot(Long schoolClassId, Long subjectId, Long semesterId, Long id);
}

