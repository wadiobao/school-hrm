package com.kltn.school_hrm.module.academic.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.academic.entity.SchoolClass;
import com.kltn.school_hrm.shared.enums.Enums.ClassStatus;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {

    List<SchoolClass> findByAcademicYearId(Long academicYearId);

    List<SchoolClass> findByAcademicYearIdAndGradeLevel(Long academicYearId, Integer gradeLevel);

    List<SchoolClass> findByStatus(ClassStatus status);

    Optional<SchoolClass> findByNameAndAcademicYearId(String name, Long academicYearId);

    boolean existsByNameAndAcademicYearId(String name, Long academicYearId);

    boolean existsByNameAndAcademicYearIdAndIdNot(String name, Long academicYearId, Long id);
}

