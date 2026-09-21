package com.kltn.school_hrm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.attendance.Shift;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    Optional<Shift> findByCode(String code);

    List<Shift> findByIsActiveTrue();
}
