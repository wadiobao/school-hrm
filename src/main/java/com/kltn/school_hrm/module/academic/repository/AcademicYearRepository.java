package com.kltn.school_hrm.module.academic.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.academic.entity.AcademicYear;
import com.kltn.school_hrm.shared.enums.Enums.AcademicStatus;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {

    Optional<AcademicYear> findByName(String name);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Optional<AcademicYear> findFirstByStatus(AcademicStatus status);
}

