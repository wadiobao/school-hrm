package com.kltn.school_hrm.module.academic.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.academic.entity.Subject;
import com.kltn.school_hrm.shared.enums.Enums.SubjectStatus;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Optional<Subject> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    List<Subject> findByStatus(SubjectStatus status);
}

