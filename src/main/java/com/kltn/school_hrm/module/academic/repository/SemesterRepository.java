package com.kltn.school_hrm.module.academic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.academic.entity.Semester;
import com.kltn.school_hrm.shared.enums.Enums.AcademicStatus;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {

    List<Semester> findByAcademicYearId(Long academicYearId);

    List<Semester> findByAcademicYearIdOrderByStartDateAsc(Long academicYearId);

    List<Semester> findByStatus(AcademicStatus status);
}

